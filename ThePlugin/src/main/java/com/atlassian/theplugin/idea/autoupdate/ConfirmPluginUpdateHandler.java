package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.util.InfoServer;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-20
 * Time: 11:30:45
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmPluginUpdateHandler implements Runnable {
	private static ConfirmPluginUpdateHandler instance;
	private InfoServer.VersionInfo versionInfo;
	private PluginUpdateIcon display;

	public void run() {
		if (display != null) {
			display.triggerUpdateAvailableAction(versionInfo);
		}
	}	

	public static synchronized ConfirmPluginUpdateHandler getInstance() {
		if (instance == null) {
			instance = new ConfirmPluginUpdateHandler();
		}
		return instance;
	}

	public void setNewVersionInfo(InfoServer.VersionInfo newVersionInfo) {
		this.versionInfo = newVersionInfo;
	}

	public void setDisplay(PluginUpdateIcon display) {
		this.display = display;
	}
}
