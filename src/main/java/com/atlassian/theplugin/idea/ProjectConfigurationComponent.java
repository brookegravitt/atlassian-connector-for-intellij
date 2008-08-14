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
import com.atlassian.theplugin.commons.cfg.CfgManagerSingleton;
import com.atlassian.theplugin.commons.cfg.xstream.JDomProjectConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class ProjectConfigurationComponent implements ProjectComponent, SettingsSavingComponent {

	private final Project project;
	private static final String CFG_LOAD_ERROR_MSG = "Error while loading Atlassian Plugin configuration.";

	public ProjectConfigurationComponent(final Project project) {
		this.project = project;
	}


	public static void handleServerCfgFactoryException(Project theProject, final Exception e) {
		Messages.showWarningDialog(theProject, CFG_LOAD_ERROR_MSG + "\n" + e.getMessage()
				+ "\nEmpty configuration will be used.", CFG_LOAD_ERROR_MSG);
	}

	public void projectOpened() {
		load();
	}


	public void projectClosed() {
		CfgManagerSingleton.getCfgManager().removeProject(CfgUtil.GLOBAL_PROJECT);
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


	public void load() {
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
		CfgManagerSingleton.getCfgManager().updateProjectConfiguration(CfgUtil.GLOBAL_PROJECT, projectConfiguration);

	}

	private void setDefaultProjectConfiguration() {
		CfgManagerSingleton.getCfgManager().updateProjectConfiguration(CfgUtil.GLOBAL_PROJECT,
				ProjectConfiguration.emptyConfiguration());
	}

	private String getCfgFilePath() {
		final VirtualFile baseDir = project.getBaseDir();
		if (baseDir == null) {
			return null;
		}
		return baseDir.getPath() + File.separator + "atlassian-ide-plugin.xml";
	}

	private String getPrivateCfgFilePath() {
		final VirtualFile baseDir = project.getBaseDir();
		if (baseDir == null) {
			return null;
		}
		return baseDir.getPath() + File.separator + "atlassian-ide-plugin.private.xml";
	}


	public void save() {
		final Element element = new Element("atlassian-ide-plugin");
		final Element privateElement = new Element("atlassian-ide-plugin-private");
		JDomProjectConfigurationFactory cfgFactory = new JDomProjectConfigurationFactory(element, privateElement);
		final ProjectConfiguration configuration = CfgManagerSingleton.getCfgManager()
				.getProjectConfiguration(CfgUtil.GLOBAL_PROJECT);
		if (configuration != null) {
			cfgFactory.save(configuration);
		}
		final String publicCfgFile = getCfgFilePath();
		final String privateCfgFile = getPrivateCfgFilePath();
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
}
