package com.atlassian.theplugin.util;

import com.atlassian.theplugin.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.idea.IdeaActionScheduler;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;
import com.atlassian.theplugin.idea.bamboo.BambooTableToolWindowPanel;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.idea.config.GeneralConfigPanel;
import com.atlassian.theplugin.idea.config.serverconfig.BambooGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.JiraGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.ServerConfigPanel;
import com.atlassian.theplugin.idea.config.serverconfig.ServerTreePanel;
import com.atlassian.theplugin.idea.config.serverconfig.util.CrucibleGeneralForm;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
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
	};

	private static final Class[] PROJECT_COMPONENTS = {
			BambooTableToolWindowPanel.class
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
