package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.util.InfoServer;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

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
	private static final Category LOGGER = Logger.getInstance(PluginStatusBarToolTip.class);

	public void run() {
		display.triggerUpdateAvailableAction(versionInfo);
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
