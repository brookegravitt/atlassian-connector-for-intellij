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
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.cfg.xstream.JDomProjectConfigurationFactory;
import com.atlassian.theplugin.idea.config.ProjectConfigurationPanel;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Document;
import org.jdom.Element;
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

public class ProjectConfigurationComponent implements ProjectComponent, SettingsSavingComponent, Configurable,
		ConfigurationListener {

	private final Project project;
	private final CfgManager cfgManager;
	private static final String CFG_LOAD_ERROR_MSG = "Error while loading Atlassian IntelliJ Connector configuration.";
	private static final Icon PLUGIN_SETTINGS_ICON = IconLoader.getIcon("/icons/ico_plugin.png");
	private ProjectConfigurationPanel projectConfigurationPanel;
	private static final String ATLASSIAN_DIR_NAME = ".atlassian";
	private static final String ATLASSIAN_IDE_CONNECTOR_DIR_NAME = "ide-connector";
	private static final String ATLASSIAN_IDE_PLUGIN_PRIVATE_OLD_XML = "atlassian-ide-plugin.private.xml";


	public ProjectConfigurationComponent(final Project project, CfgManager cfgManager) {
		this.project = project;
		this.cfgManager = cfgManager;

	}


	public static void handleServerCfgFactoryException(Project theProject, final Exception e) {
		Messages.showWarningDialog(theProject, CFG_LOAD_ERROR_MSG + "\n" + e.getMessage()
				+ "\nEmpty configuration will be used.", CFG_LOAD_ERROR_MSG);
	}

	public void projectOpened() {
		load();
		cfgManager.addProjectConfigurationListener(getProjectId(), this);
	}


	public void projectClosed() {
		cfgManager.removeProjectConfigurationListener(getProjectId(), this);
		cfgManager.removeProject(getProjectId());
		cfgManager.removeAllConfigurationCredentialListeners(getProjectId());
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
		} catch (Exception e) {
			handleServerCfgFactoryException(project, e);
			setDefaultProjectConfiguration();
			return;
		}

		Document privateRoot = null; // null means that there is no private cfg available
		try {
			final String privateCfgFile = getPrivateCfgFileLoadPath();
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
		cfgManager.updateProjectConfiguration(CfgUtil.getProjectId(project), projectConfiguration);

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

	private String getPrivateCfgFileName() {

		return  "atlassian-ide-plugin." + project.getName() + ".xml";
	}
	/*Tries to load file from
			$HOME/.atlassian/ide-connector/atlassian-ide-plugin.PROJECT_NAME.xml
	  next from
	  		project.BaseDir + atlassian-ide-connector.private.xml*/
	private String getPrivateCfgFileLoadPath() {

		final VirtualFile baseProjectDir =  LocalFileSystem.getInstance()
				.findFileByPath(project.getBaseDir().getPath() + File.separator + ATLASSIAN_IDE_PLUGIN_PRIVATE_OLD_XML);

		final VirtualFile baseHomeDir = LocalFileSystem.getInstance()
				.findFileByPath(System.getProperty("user.home") + File.separator + ATLASSIAN_DIR_NAME + File.separator
						+ File.separator + ATLASSIAN_IDE_CONNECTOR_DIR_NAME + File.separator + getPrivateCfgFileName());

		final VirtualFile baseNewProjectDir =  LocalFileSystem.getInstance()
				.findFileByPath(project.getBaseDir().getPath() + File.separator + getPrivateCfgFileName());


		if (baseHomeDir != null) {
			return baseHomeDir.getPath();
		} else if (baseProjectDir != null) {
			return baseProjectDir.getPath();
		} else if (baseNewProjectDir != null) {
			return baseNewProjectDir.getPath();
		}

		return null;
	}

	/*Target filr in  $HOME/.atlassian/ide-connector/atlassina-ide-connector*/
	private String getPrivateCfgFileSavePath() {
		final VirtualFile baseProjectDir = project.getBaseDir();
		final VirtualFile baseHomeDir = LocalFileSystem.getInstance()
				.findFileByPath(System.getProperty("user.home"));

		if (baseHomeDir != null) {
			final VirtualFile baseHomeAtlassian = LocalFileSystem.getInstance()
					.findFileByPath(System.getProperty("user.home") + File.separator + ATLASSIAN_DIR_NAME);
			if (baseHomeAtlassian == null) {
				try {
					baseHomeDir.createChildDirectory(this, ATLASSIAN_DIR_NAME);

				} catch (IOException e) {
					IdeaLoggerImpl.getInstance().error(
							"Cannot create directory:" + baseHomeAtlassian + File.separator + ATLASSIAN_DIR_NAME + "\nerror:"
									+ e.getMessage());
					return null;
				}
			}
			final VirtualFile baseHomeAtlassianIdePlugin = LocalFileSystem.getInstance()
					.findFileByPath(System.getProperty("user.home") + File.separator + ATLASSIAN_DIR_NAME + File
							.separator + ATLASSIAN_IDE_CONNECTOR_DIR_NAME);
			if (baseHomeAtlassianIdePlugin == null) {
				try {
					baseHomeAtlassian.createChildDirectory(this, ATLASSIAN_IDE_CONNECTOR_DIR_NAME);
				} catch (IOException e) {
					IdeaLoggerImpl.getInstance().error(
							"Cannot create directory:" + baseHomeAtlassian + File.separator + ATLASSIAN_IDE_CONNECTOR_DIR_NAME
									+ "\nerror:" + e.getMessage());
					return null;
				}
			}
			return baseHomeDir.getPath() + File.separator + ATLASSIAN_DIR_NAME + File.separator
					+ ATLASSIAN_IDE_CONNECTOR_DIR_NAME + File.separator + getPrivateCfgFileName();

		} else if (baseProjectDir != null) {
			return baseProjectDir.getPath() + File.separator + getPrivateCfgFileName();
		}

		return null;
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
		}
		final String publicCfgFile = getCfgFilePath();
		final String privateCfgFile = getPrivateCfgFileSavePath();
		writeXmlFile(element, publicCfgFile);
		writeXmlFile(privateElement, privateCfgFile);
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
		projectConfigurationPanel = new ProjectConfigurationPanel(project, configuration.getClone());
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

	public void configurationUpdated(final ProjectConfiguration aProjectConfiguration) {
		save();
		IdeaHelper.getAppComponent().rescheduleStatusCheckers(true);
	}

	public void projectUnregistered() {
	}
}
