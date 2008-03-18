package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.exception.ThePluginException;
import com.intellij.openapi.application.ApplicationManager;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 13, 2008
 * Time: 12:17:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ForwardToIconHandler implements UpdateActionHandler {
	private PluginConfiguration pluginConfiguration;

	public ForwardToIconHandler(PluginConfiguration pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
	}

	public void doAction(InfoServer.VersionInfo versionInfo) throws ThePluginException {
		if (!versionInfo.getVersion().equals(pluginConfiguration.getRejectedUpgrade())) {
			ConfirmPluginUpdateHandler handler = ConfirmPluginUpdateHandler.getInstance();
			handler.setNewVersionInfo(versionInfo);
			ApplicationManager.getApplication().invokeLater(handler);
		}
	}
}
