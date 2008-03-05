package com.atlassian.theplugin.util;

import com.atlassian.theplugin.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.idea.IdeaActionScheduler;
import com.atlassian.theplugin.idea.NewVersionChecker;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.idea.config.GeneralConfigPanel;
import com.atlassian.theplugin.idea.config.serverconfig.BambooGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.ServerConfigPanel;
import com.atlassian.theplugin.idea.config.serverconfig.ServerTreePanel;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.crucible.CrucibleServerFacadeImpl;
import org.picocontainer.MutablePicoContainer;

public final class PicoUtil {
	///CLOVER:OFF
	private PicoUtil() {
	}
	///CLOVER:ON

	private static Class[] registeredComponents = {
			IdeaActionScheduler.class,
			PluginConfigurationBean.class,
			BambooStatusChecker.class,
			CrucibleStatusChecker.class,
			NewVersionChecker.class,
			ConfigPanel.class,
			ServerConfigPanel.class,
			BambooGeneralForm.class,
			GeneralConfigPanel.class,
			ServerTreePanel.class,
			CrucibleServerFacadeImpl.class,
	};

	public static void populatePicoContainer(MutablePicoContainer pico) {
		for (Class clazz : registeredComponents) {
			pico.registerComponentImplementation(clazz);
		}
	}
}
