/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.cfg.xstream.JDomProjectConfigurationDao;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacadeImpl;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ProjectConfigurationComponent implements ProjectComponent, SettingsSavingComponent, Configurable {

	private final Project project;
	private final ProjectCfgManagerImpl projectCfgManager;
	private final UiTaskExecutor uiTaskExecutor;
	private final PrivateConfigurationDao privateCfgDao;
	private final WorkspaceConfigurationBean projectConfigurationBean;
	private static final String CFG_LOAD_ERROR_MSG = "Error while loading Atlassian IntelliJ Connector configuration.";
	private static final Icon PLUGIN_SETTINGS_ICON = IconLoader.getIcon("/icons/ico_plugin.png");
	private ProjectConfigurationPanel projectConfigurationPanel;
	private LocalConfigurationListener configurationListener = new LocalConfigurationListener();
	/**
	 * race condtions wrt to this variable are harmless as threads mutating it (via save) reset it from false to true
	 * and then save configuration. So saving (if really needed) will not be skipped anyway even if two threads
	 * start in the moment when shouldSaveConfiguration was false
	 */
	private boolean shouldSaveConfiguration;
	private ServerCfg selectedServer;


	public ProjectConfigurationComponent(final Project project, final ProjectCfgManagerImpl projectCfgManager,
			final UiTaskExecutor uiTaskExecutor,
			@NotNull PrivateConfigurationDao privateCfgDao, @NotNull WorkspaceConfigurationBean projectConfigurationBean) {
		this.project = project;
		this.projectCfgManager = projectCfgManager;
		this.uiTaskExecutor = uiTaskExecutor;
		this.privateCfgDao = privateCfgDao;
		this.projectConfigurationBean = projectConfigurationBean;
		shouldSaveConfiguration = load();
	}


	public static void handleServerCfgFactoryException(final Project theProject, final Exception e) {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				DialogWithDetails.showExceptionDialog(theProject, CFG_LOAD_ERROR_MSG + "\nEmpty configuration will be used.\n"
						+ "If you want to preserve settings stored in your configuration, do not edit your "
						+ PluginUtil.PRODUCT_NAME + " configuration using IDEA, but instead close the project "
						+ "and try resolve the problem by modyfing directly the configuration file", e);
			}
		});
	}

	public void projectOpened() {
		if (projectCfgManager.getProjectConfiguration() == null) {
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					Messages.showErrorDialog(project, "If you see this message, something bad happend to the "
							+ "initialization sequence of IDEA. You may encounter now various strange problems with Connector."
							+ "\nPlease report occurence of this message to us.",
							"Internal Error in Atlassian IntelliJ Connector");
				}
			});
		}

		projectCfgManager.addProjectConfigurationListener(configurationListener);
	}


	public void projectClosed() {
		projectCfgManager.removeProjectConfigurationListener(configurationListener);
		projectCfgManager.removeProject();
//		projectCfgManager.removeAllConfigurationCredentialListeners(getProjectId());
	}

	@NonNls
	@NotNull
	public String getComponentName() {
		return ProjectConfigurationComponent.class.getSimpleName();
	}

	public void initComponent() {
	}

	public void disposeComponent() {
	}


	private boolean load() {
		final Document root;
		final SAXBuilder builder = new SAXBuilder(false);
		final ProjectId projectId = CfgUtil.getProjectId(project);
		try {
			final String path = getCfgFilePath();
			if (path == null || !new File(path).exists()) {
				// this is an empty project (default template used by IDEA)
				setDefaultProjectConfiguration();
				return false;
			}
			root = builder.build(path);
			cleanupDom(root);
		} catch (Exception e) {
			handleServerCfgFactoryException(project, e);
			setDefaultProjectConfiguration();
			return false;
		}

		final JDomProjectConfigurationDao cfgFactory = new JDomProjectConfigurationDao(root.getRootElement(), privateCfgDao);
		migrateOldPrivateProjectSettings(cfgFactory);

		try {
			final ProjectConfiguration projectConfiguration;
			projectConfiguration = cfgFactory.load();
			if (projectConfiguration.getDefaultFishEyeServer() == null) {
				//means that configuration holds Crucible as FishEye server.
				//in the future this code should be removed
				//now resolves migration problem from Crucible as FishEye to pure FishEye
				projectConfiguration.setDefaultFishEyeServerId(null);
			}
			projectCfgManager.updateProjectConfiguration(projectConfiguration);
		} catch (ServerCfgFactoryException e) {
			handleServerCfgFactoryException(project, e);
			setDefaultProjectConfiguration();
			return false;
		}


		return true;
	}

	private void migrateOldPrivateProjectSettings(final JDomProjectConfigurationDao cfgFactory) {
		final SAXBuilder builder = new SAXBuilder(false);
		final File privateCfgFile = getPrivateOldCfgFilePath();
		boolean someMigrationHappened = false;
		if (privateCfgFile != null) {
			try {
				final Document privateRoot = builder.build(privateCfgFile);
				final PrivateProjectConfiguration ppc = cfgFactory.loadOldPrivateConfiguration(privateRoot.getRootElement());
				for (PrivateServerCfgInfo privateServerCfgInfo : ppc.getPrivateServerCfgInfos()) {
					try {
						final PrivateServerCfgInfo newPsci = privateCfgDao.load(privateServerCfgInfo.getServerId());
						if (newPsci == null) {
							privateCfgDao.save(privateServerCfgInfo);
							someMigrationHappened = true;
						}
					} catch (ServerCfgFactoryException e) {
						// ignore here - just don't try to overwrite it with data from old XML file
					}
				}
			} catch (Exception e) {
				handleServerCfgFactoryException(project, e);
			}
		}

		if (someMigrationHappened) {
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					int value = Messages.showYesNoCancelDialog(project,
							"Configuration has been succesfully migrated to new location (home directory).\n"
									+ "Would you like to delete the old configuration file?\n\nDelete file: ["
									+ privateCfgFile + "]",
							PluginUtil.PRODUCT_NAME + " upgrade process", Messages.getQuestionIcon());

					if (value == DialogWrapper.OK_EXIT_CODE) {
						if (!privateCfgFile.delete()) {
							Messages.showWarningDialog(project, "Cannot remove file [" + privateCfgFile.getAbsolutePath()
									+ "].\nTry removing it manually.\n" + PluginUtil.PRODUCT_NAME
									+ " should still behave correctly.", PluginUtil.PRODUCT_NAME);
						}

					}
				}
			});
		}

	}

	/**
	 * Ensuring that old attributes do not break our loading
	 *
	 * @param root root element of XML document
	 * @throws org.jdom.JDOMException when tree is invalid
	 */
	private void cleanupDom(final Document root) throws JDOMException {
		@SuppressWarnings("unchecked")
		List<Element> nodes = XPath.selectNodes(root, "atlassian-ide-plugin/project-configuration/servers/crucible");
		for (Element e : nodes) {
			e.removeChild("projectName");
			e.removeChild("repositoryName");
		}
		@SuppressWarnings("unchecked")
		final List<Element> prjCfgNode = XPath.selectNodes(root, "atlassian-ide-plugin/project-configuration");
		for (Element e : prjCfgNode) {
			e.removeChild("isPrivateConfigurationMigrated");
			e.removeChild("defaultJiraServerId");
			e.removeChild("defaultJiraProject");
		}
		/** there was defaultUser (by mistake) child of server element */
		@SuppressWarnings("unchecked")
		List<Element> serverNodes = XPath.selectNodes(root, "atlassian-ide-plugin/project-configuration/servers/*");
		for (Element e : serverNodes) {
			e.removeChild("defaultUser");
		}

		@SuppressWarnings("unchecked")
		List<Element> projectConfigurationNode = XPath.selectNodes(root, "atlassian-ide-plugin/project-configuration");
		for (Element e : projectConfigurationNode) {
			e.removeChild("defaultUser");
		}
	}

	private ProjectConfiguration setDefaultProjectConfiguration() {
		final ProjectConfiguration configuration = ProjectConfiguration.emptyConfiguration();
		projectCfgManager.updateProjectConfiguration(configuration);
		return configuration;
	}

	@Nullable
	private String getCfgFilePath() {
		final VirtualFile baseDir = project.getBaseDir();
		if (baseDir == null) {
			return null;
		}
		return baseDir.getPath() + File.separator + "atlassian-ide-plugin.xml";
	}


	private File getPrivateOldCfgFilePath() {
		final VirtualFile baseDir = project.getBaseDir();
		if (baseDir == null) {
			return null;
		}
		final File baseNewProjectFile = new File(baseDir.getPath()
				+ File.separator + "atlassian-ide-plugin.private.xml");

		if (baseNewProjectFile.isFile() && baseNewProjectFile.canRead()) {
			return baseNewProjectFile;
		}

		return null;
	}

//	private ProjectId getProjectId() {
//		return CfgUtil.getProjectId(project);
//	}

	public void save() {
		final Element element = new Element("atlassian-ide-plugin");

		JDomProjectConfigurationDao cfgFactory = new JDomProjectConfigurationDao(element, privateCfgDao);
		final ProjectConfiguration configuration = projectCfgManager.getProjectConfiguration();
		if (configuration != null) {
			if (configuration.getServers().size() > 0 && shouldSaveConfiguration == false) {
				// apparently somebody still prefers to populate invalid configuration, so we would save it now
				shouldSaveConfiguration = true;
			}

			if (shouldSaveConfiguration) {
				cfgFactory.save(configuration);
				final String publicCfgFile = getCfgFilePath();

				writeXmlFile(element, publicCfgFile);

			}
		}
	}


	private void writeXmlFile(final Element element, final String filepath) {
		if (filepath == null) {
			return; // handlig for instance default dummy project
		}
		try {
			final FileWriter writer = new FileWriter(filepath);
			new XMLOutputter(Format.getPrettyFormat()).output(element, writer);
			writer.close();
		} catch (IOException e) {
			Messages.showWarningDialog(project, "Cannot save project configuration settings to [" + filepath + ":\n"
					+ e.getMessage(), "Error");
		}
	}

	@Nls
	public String getDisplayName() {
		return "Atlassian\nConnector";
	}

	@Nullable
	public Icon getIcon() {
		return PLUGIN_SETTINGS_ICON;
	}

	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	public JComponent createComponent() {
		ProjectConfiguration configuration = projectCfgManager.getProjectConfiguration();
		if (configuration == null) {
			// may happen for Default Template project
			configuration = setDefaultProjectConfiguration();
		}
		projectConfigurationPanel = new ProjectConfigurationPanel(project, configuration.getClone(),
				CrucibleServerFacadeImpl.getInstance(), FishEyeServerFacadeImpl.getInstance(),
				BambooServerFacadeImpl.getInstance(PluginUtil.getLogger()), JIRAServerFacadeImpl.getInstance(), uiTaskExecutor,
				selectedServer, projectCfgManager.getDefaultCredentials().getClone(),
				projectCfgManager.isDefaultCredentialsAsked(), projectConfigurationBean);
		return projectConfigurationPanel;
	}

	public boolean isModified() {
		projectConfigurationPanel.saveData(false);
		return !(projectCfgManager.getProjectConfiguration().equals(projectConfigurationPanel.getProjectConfiguration())
				&& projectCfgManager.getDefaultCredentials().equals(projectConfigurationPanel.getDefaultCredentials())
				&& projectCfgManager.isDefaultCredentialsAsked() == projectConfigurationPanel.isDefaultCredentialsAsked());
	}

	public void apply() throws ConfigurationException {
		projectConfigurationPanel.askForDefaultCredentials();
		if (projectConfigurationPanel == null) {
			return;
		}
		projectConfigurationPanel.saveData(true);
		projectCfgManager.updateProjectConfiguration(projectConfigurationPanel.getProjectConfiguration());
		projectConfigurationPanel.setData(projectCfgManager.getProjectConfiguration().getClone());
		projectCfgManager.setDefaultCredentials(projectConfigurationPanel.getDefaultCredentials());
		projectCfgManager.setDefaultCredentialsAsked(projectConfigurationPanel.isDefaultCredentialsAsked());
	}

	public void reset() {
	}

	public void disposeUIResources() {
		projectConfigurationPanel = null;
	}

	public void setSelectedServer(final ServerCfg server) {
		this.selectedServer = server;
	}

	private class LocalConfigurationListener extends ConfigurationListenerAdapter {
		@Override
		public void configurationUpdated(ProjectConfiguration aProjectConfiguration) {
			save();
			IdeaHelper.getAppComponent().rescheduleStatusCheckers(true);
		}
	}
}
