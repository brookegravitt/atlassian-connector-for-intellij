package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PasswordDialog;
import com.atlassian.theplugin.idea.ThePluginApplicationComponent;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

/**
 * Shows a dialog for each Bamboo server that has not the password set.
 */
public class MissingPasswordHandler implements Runnable {

	private static boolean isDialogShown = false;

	public void run() {

		if (!isDialogShown) {

			isDialogShown = true;
			boolean wasCanceled = false;

			for (Server server
					: ConfigurationFactory.getConfiguration().getProductServers(ServerType.BAMBOO_SERVER).getServers()) {
				if (server.getIsConfigInitialized()) {
					continue;
				}
				PasswordDialog dialog = new PasswordDialog(server);
				dialog.setUserName(server.getUserName());
				dialog.pack();
				JPanel panel = dialog.getPasswordPanel();

				int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), panel,
						PluginUtil.getName(), OK_CANCEL_OPTION, PLAIN_MESSAGE);

				if (answer == JOptionPane.OK_OPTION) {
					String password = dialog.getPasswordString();
					Boolean shouldPasswordBeStored = dialog.getShouldPasswordBeStored();
					server.setPasswordString(password, shouldPasswordBeStored);
					server.setUserName(dialog.getUserName());
				} else {
					wasCanceled = true;
				}
				// so or so we assume that user provided password

				server.setIsConfigInitialized(true);
			}
			ThePluginApplicationComponent appComponent = IdeaHelper.getAppComponent();
			appComponent.rescheduleStatusCheckers(true);

			if (wasCanceled) {
				Messages.showMessageDialog(
						"You can always change password by changing plugin settings (Preferences | IDE Settings | "
								+ PluginUtil.getName() + ")", "Information", Messages.getInformationIcon());
			}
			isDialogShown = false;
		}

	}
}
