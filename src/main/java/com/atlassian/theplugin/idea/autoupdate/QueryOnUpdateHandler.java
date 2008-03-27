package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.exception.ThePluginException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.Version;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 13, 2008
 * Time: 1:27:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryOnUpdateHandler implements UpdateActionHandler {
	private static final String DOWNLOAD_TITLE = "Downloading new " + PluginUtil.getName() + " plugin version ";
	public QueryOnUpdateHandler(PluginConfiguration pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
	}

	private PluginConfiguration pluginConfiguration;

	public void doAction(final InfoServer.VersionInfo versionInfo) throws ThePluginException {
		Version aVersion = versionInfo.getVersion();
		String message = "New plugin version " + aVersion + " is available. "
				+ "Your version is " + PluginUtil.getVersion()
				+ ". Do you want to download and install?";
		String title = "New plugin version download";

		int answer = Messages.showYesNoDialog(message, title, Messages.getQuestionIcon());

		//int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
		//		message, title, JOptionPane.YES_NO_OPTION);
		//if (answer == JOptionPane.OK_OPTION) {
		if (answer == DialogWrapper.OK_EXIT_CODE) {
			// fire downloading and updating plugin in the new thread
			//Thread downloader = new Thread(new PluginDownloader(versionInfo, pluginConfiguration));
			//downloader.start();

			Task.Backgroundable downloader = new Task.Backgroundable(IdeaHelper.getCurrentProject(), DOWNLOAD_TITLE, false) {
				public void run(ProgressIndicator indicator) {
					new PluginDownloader(versionInfo, pluginConfiguration).run();
				}
			};

			ProgressManager.getInstance().run(downloader);
		} else {
			Messages.showMessageDialog("You can always install " + aVersion
					+ " version through " + PluginUtil.getName()
					+ " configuration panel (Preferences | IDE Settings | "
					+ PluginUtil.getName() + " | General | Auto update | Check now)", "Information",
					Messages.getInformationIcon());
		}
		// so or so we mark this version so no more popups will appear
		pluginConfiguration.setRejectedUpgrade(versionInfo.getVersion());
	}
}
