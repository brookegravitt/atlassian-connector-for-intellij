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
package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.cfg.xstream.JDomProjectConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacadeImpl;
import com.atlassian.theplugin.idea.config.ProjectConfigurationPanel;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
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
	private final CfgManager cfgManager;
	private final UiTaskExecutor uiTaskExecutor;
	private static final String CFG_LOAD_ERROR_MSG = "Error while loading Atlassian IntelliJ Connector configuration.";
	private static final Icon PLUGIN_SETTINGS_ICON = IconLoader.getIcon("/icons/ico_plugin.png");
	private ProjectConfigurationPanel projectConfigurationPanel;
	private LocalConfigurationListener configurationListener = new LocalConfigurationListener();

	public ProjectConfigurationComponent(final Project project, final CfgManager cfgManager,
			final UiTaskExecutor uiTaskExecutor) {
		this.project = project;
		this.cfgManager = cfgManager;
		this.uiTaskExecutor = uiTaskExecutor;
	}


	public static void handleServerCfgFactoryException(Project theProject, final Exception e) {
		DialogWithDetails.showExceptionDialog(theProject, CFG_LOAD_ERROR_MSG + "\nEmpty configuration will be used.",
				e, CFG_LOAD_ERROR_MSG);
	}

	public void projectOpened() {
		load();
		cfgManager.addProjectConfigurationListener(getProjectId(), configurationListener);
	}


	public void projectClosed() {
		cfgManager.removeProjectConfigurationListener(getProjectId(), configurationListener);
		cfgManager.removeProject(getProjectId());
//		cfgManager.removeAllConfigurationCredentialListeners(getProjectId());
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


	private void load() {
		final Document root;
		final SAXBuilder builder = new SAXBuilder(false);
		try {
			final String path = getCfgFilePath();
			if (new File(path).exists() == false) {
				setDefaultProjectConfiguration();
				return;
			}
			root = builder.build(path);
			cleanupDom(root);
		} catch (Exception e) {
			handleServerCfgFactoryException(project, e);
			setDefaultProjectConfiguration();
			return;
		}

		Document privateRoot = null; // null means that there is no private cfg available
		try {
			final String privateCfgFile = getPrivateCfgFilePath();
			if (new File(privateCfgFile).exists()) {
				privateRoot = builder.build(privateCfgFile);
			}
		} catch (Exception e) {
			handleServerCfgFactoryException(project, e);
		}

		ProjectConfigurationFactory cfgFactory = new JDomProjectConfigurationFactory(root.getRootElement(),
				privateRoot != null ? privateRoot.getRootElement() : null);
		ProjectConfiguration projectConfiguration;
		try {
			projectConfiguration = cfgFactory.load();
		} catch (ServerCfgFactoryException e) {
			handleServerCfgFactoryException(project, e);
			setDefaultProjectConfiguration();
			return;
		}
		if (projectConfiguration.getDefaultFishEyeServer() == null) {
			//means that configuration holds Crucible as FishEye server.
			//in the future this code should be removed
			//now resolves migration problem from Crucible as FishEye to pure FishEye
			projectConfiguration.setDefaultFishEyeServerId(null);
		}
		cfgManager.updateProjectConfiguration(CfgUtil.getProjectId(project), projectConfiguration);

	}

	/**
	 * Ensuring that old attributes do not break our loading
	 */
	private void cleanupDom(final Document root) throws JDOMException {
		@SuppressWarnings("unchecked")
		List<Element> nodes = XPath.selectNodes(root, "atlassian-ide-plugin/project-configuration/servers/crucible");
		for (Element e : nodes) {
			e.removeChild("projectName");
			e.removeChild("repositoryName");
		}
	}

	private ProjectConfiguration setDefaultProjectConfiguration() {
		final ProjectConfiguration configuration = ProjectConfiguration.emptyConfiguration();
		cfgManager.updateProjectConfiguration(CfgUtil.getProjectId(project),
				configuration);
		return configuration;
	}

	private String getCfgFilePath() {
		final VirtualFile baseDir = project.getBaseDir();
		if (baseDir == null) {
			return null;
		}
		return baseDir.getPath() + File.separator + "atlassian-ide-plugin.xml";
	}

	@Nullable
	private String getPrivateCfgFilePath() {
		final VirtualFile baseDir = project.getBaseDir();
		if (baseDir == null) {
			return null;
		}
		return baseDir.getPath() + File.separator + "atlassian-ide-plugin.private.xml";
	}


	private ProjectId getProjectId() {
		return CfgUtil.getProjectId(project);
	}

	public void save() {
		final Element element = new Element("atlassian-ide-plugin");
		final Element privateElement = new Element("atlassian-ide-plugin-private");
		JDomProjectConfigurationFactory cfgFactory = new JDomProjectConfigurationFactory(element, privateElement);
		final ProjectConfiguration configuration = cfgManager.getProjectConfiguration(getProjectId());
		if (configuration != null) {
			cfgFactory.save(configuration);
			final String publicCfgFile = getCfgFilePath();
			final String privateCfgFile = getPrivateCfgFilePath();
			writeXmlFile(element, publicCfgFile);
			writeXmlFile(privateElement, privateCfgFile);
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
		ProjectConfiguration configuration = cfgManager.getProjectConfiguration(getProjectId());
		if (configuration == null) {
			// may happen for Default Template project
			configuration = setDefaultProjectConfiguration();
		}
		projectConfigurationPanel = new ProjectConfigurationPanel(project, configuration.getClone(),
				CrucibleServerFacadeImpl.getInstance(), FishEyeServerFacadeImpl.getInstance(), uiTaskExecutor);
		return projectConfigurationPanel;
	}

	public boolean isModified() {
		projectConfigurationPanel.saveData(false);
		return !cfgManager.getProjectConfiguration(getProjectId()).equals(projectConfigurationPanel.getProjectConfiguration());
	}

	public void apply() throws ConfigurationException {
		if (projectConfigurationPanel == null) {
			return;
		}
		projectConfigurationPanel.saveData(true);
		cfgManager.updateProjectConfiguration(getProjectId(), projectConfigurationPanel.getProjectConfiguration());
		projectConfigurationPanel.setData(cfgManager.getProjectConfiguration(getProjectId()).getClone());
	}

	public void reset() {
	}

	public void disposeUIResources() {
		projectConfigurationPanel = null;
	}

	private class LocalConfigurationListener extends ConfigurationListenerAdapter {
		@Override
		public void configurationUpdated(ProjectConfiguration aProjectConfiguration) {
			save();
			IdeaHelper.getAppComponent().rescheduleStatusCheckers(true);
		}
	}
}
