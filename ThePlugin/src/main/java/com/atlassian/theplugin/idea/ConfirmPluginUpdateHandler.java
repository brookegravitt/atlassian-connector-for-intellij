package com.atlassian.theplugin.idea;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-20
 * Time: 11:30:45
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmPluginUpdateHandler implements Runnable {
	private String version;

	private static boolean isTriggered = false;

	public ConfirmPluginUpdateHandler(String version) {
		this.version = version;
	}

	public void run() {

		if (!isTriggered) {

			String message = "New plugin version " + version + " is available. "
					+ "Your version is " + PluginInfoUtil.getVersion()
					+ ". Do you want to donwload and install?";
			String title = "New plugin version download";

			isTriggered = true;

			int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
					message, title, JOptionPane.YES_NO_OPTION);

			if (answer == JOptionPane.OK_OPTION) {

				// fire downloading and updating plugin in the new thread
				Thread downloader = new Thread(new PluginDownloader(version));

				downloader.start();
			}
		}
	}
}
