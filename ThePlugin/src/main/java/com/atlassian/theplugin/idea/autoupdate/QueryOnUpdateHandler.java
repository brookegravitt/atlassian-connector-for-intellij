package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.autoupdate.PluginDownloader;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.Version;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 13, 2008
 * Time: 1:27:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryOnUpdateHandler implements UpdateActionHandler {

	public QueryOnUpdateHandler(PluginConfiguration pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
	}

	private PluginConfiguration pluginConfiguration;

	public void doAction(InfoServer.VersionInfo versionInfo) throws ThePluginException {
		Version aVersion = versionInfo.getVersion();
		String message = "New plugin version " + aVersion + " is available. "
				+ "Your version is " + PluginUtil.getVersion()
				+ ". Do you want to download and install?";
		String title = "New plugin version download";

		int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
				message, title, JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.OK_OPTION) {

			// fire downloading and updating plugin in the new thread
			//Thread downloader = new Thread(new PluginDownloader(versionInfo, pluginConfiguration));
			//downloader.start();

			Task.Backgroundable downloader = new PluginDownloader(versionInfo, pluginConfiguration, IdeaHelper.getCurrentProject());

			ProgressManager.getInstance().run(downloader);

		} else {
				pluginConfiguration.setRejectedUpgrade(versionInfo.getVersion());
		}

	}
}
