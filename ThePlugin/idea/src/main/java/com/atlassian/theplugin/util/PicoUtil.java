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

package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.cfg.CfgManagerImpl;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaActionScheduler;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;
import com.atlassian.theplugin.idea.bamboo.BambooBuildToolWindow;
import com.atlassian.theplugin.idea.bamboo.BambooTableToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.BuildChangesToolWindow;
import com.atlassian.theplugin.idea.bamboo.TestResultsToolWindow;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.idea.jira.editor.StackTraceConsole;
import com.atlassian.theplugin.idea.util.IdeaUiTaskExecutor;
import org.picocontainer.MutablePicoContainer;

public final class PicoUtil {
	///CLOVER:OFF
	private PicoUtil() {
	}
	///CLOVER:ON

	private static final Class<?>[] GLOBAL_COMPONENTS = {
			IdeaActionScheduler.class,
			PluginConfigurationBean.class,
			CfgManagerImpl.class,
			NewVersionChecker.class,
			IdeaUiTaskExecutor.class,
//			BambooStatusChecker.class,
//			CrucibleStatusChecker.class,
//			NewVersionChecker.class,
//			ConfigPanel.class,
//			ServerConfigPanel.class,
//			BambooGeneralForm.class,
//			CrucibleGeneralForm.class,
//			JiraGeneralForm.class,
//			GeneralConfigPanel.class,
//			ServerTreePanel.class,
//			CrucibleServerFacadeImpl.class,
//			BambooServerFacadeImpl.class,
//			JIRAServerFacadeImpl.class,
	};

	private static final Class<?>[] PROJECT_COMPONENTS = {
			BambooTableToolWindowPanel.class,
			TestResultsToolWindow.class,
			BuildChangesToolWindow.class,
			CrucibleTableToolWindowPanel.class,
			JIRAToolWindowPanel.class,
			ProjectConfigurationBean.class,
			StackTraceConsole.class,
			BambooBuildToolWindow.class
	};


	public static void populateGlobalPicoContainer(MutablePicoContainer pico) {
		populate(pico, GLOBAL_COMPONENTS);
	}

	public static void populateProjectPicoContainer(MutablePicoContainer pico) {
		populate(pico, PROJECT_COMPONENTS);
	}

	private static void populate(MutablePicoContainer pico, Class<?>[] projectComponents) {
		for (Class<?> clazz : projectComponents) {
			pico.registerComponentImplementation(clazz);
		}
	}

}
