package com.atlassian.theplugin.util;

import com.atlassian.theplugin.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.idea.IdeaActionScheduler;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;
import com.atlassian.theplugin.idea.bamboo.BambooTableToolWindowPanel;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.idea.config.GeneralConfigPanel;
import com.atlassian.theplugin.idea.config.serverconfig.*;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import org.picocontainer.MutablePicoContainer;

public final class PicoUtil {
	///CLOVER:OFF
	private PicoUtil() {
	}
	///CLOVER:ON

	private static final Class[] GLOBAL_COMPONENTS = {
			IdeaActionScheduler.class,
			PluginConfigurationBean.class,
			BambooStatusChecker.class,
			CrucibleStatusChecker.class,
			NewVersionChecker.class,
			ConfigPanel.class,
			ServerConfigPanel.class,
			BambooGeneralForm.class,
			CrucibleGeneralForm.class,
			JiraGeneralForm.class,
			GeneralConfigPanel.class,
			ServerTreePanel.class,
			CrucibleServerFacadeImpl.class,
			BambooServerFacadeImpl.class,
			JIRAServerFacadeImpl.class,
	};

	private static final Class[] PROJECT_COMPONENTS = {
			BambooTableToolWindowPanel.class,
			CrucibleTableToolWindowPanel.class,
			JIRAToolWindowPanel.class,
			ProjectConfigurationBean.class
	};


	public static void populateGlobalPicoContainer(MutablePicoContainer pico) {
		populate(pico, GLOBAL_COMPONENTS);
	}

	public static void populateProjectPicoContainer(MutablePicoContainer pico) {
		populate(pico, PROJECT_COMPONENTS);
	}

	private static void populate(MutablePicoContainer pico, Class[] projectComponents) {
		for (Class clazz : projectComponents) {
			pico.registerComponentImplementation(clazz);
		}
	}

}
