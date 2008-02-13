package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.idea.PasswordDialog;
import com.atlassian.theplugin.idea.PluginInfoUtil;
import com.atlassian.theplugin.idea.ThePluginApplicationComponent;
import com.intellij.openapi.application.ApplicationManager;

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

			for (Server server : ConfigurationFactory.getConfiguration().getProductServers(ServerType.BAMBOO_SERVER).getServers()) {
				if (server.getIsConfigInitialized()) {
					continue;
				}
				ServerBean serverBean = (ServerBean) server;
				PasswordDialog dialog = new PasswordDialog(server);
				dialog.setUserName(serverBean.getUsername());
				dialog.pack();
				JPanel panel = dialog.getPasswordPanel();

				int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), panel,
														   PluginInfoUtil.getName(), OK_CANCEL_OPTION, PLAIN_MESSAGE);

				if (answer == JOptionPane.OK_OPTION) {
					String password = dialog.getPasswordString();
					Boolean shouldPasswordBeStored = dialog.getShouldPasswordBeStored();
					serverBean.setPasswordString(password, shouldPasswordBeStored);
					serverBean.setUsername(dialog.getUserName());
				} else {

					JOptionPane.showMessageDialog(null,
												  "You can always change password by changing plugin settings (Preferences | IDE Settings | "
														  + PluginInfoUtil.getName() + ")");
				}
				// so or so we assume that user provided password

				serverBean.setIsConfigInitialized(true);
			}
			ThePluginApplicationComponent appComponent =
					ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);
			appComponent.triggerStatusCheckers();

			isDialogShown = false;
		}

	}
}
