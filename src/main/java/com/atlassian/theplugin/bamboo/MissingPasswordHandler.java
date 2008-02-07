package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.idea.PasswordDialog;
import com.atlassian.theplugin.idea.PluginInfoUtil;
import com.atlassian.theplugin.idea.ThePluginApplicationComponent;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerType;
import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-25
 * Time: 11:20:12
 * To change this template use File | Settings | File Templates.
 */
public class MissingPasswordHandler implements Runnable {

	private static boolean isDialogShown = false;

	public void run() {

		//ServerBean conf = (ServerBean) (ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer());

		if (!isDialogShown) {

			isDialogShown = true;

			for (Server server : ConfigurationFactory.getConfiguration().getProductServers(ServerType.BAMBOO_SERVER).getServers()) {
				ServerBean serverBean = (ServerBean) server;
				PasswordDialog dialog = new PasswordDialog(server);
				dialog.setUserName(serverBean.getUsername());
				dialog.pack();
				JPanel panel = dialog.getPasswordPanel();

				//WindowManager.getInstance().getAllFrames();

				int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), panel,
						PluginInfoUtil.getName(), OK_CANCEL_OPTION, PLAIN_MESSAGE);

				String password = "";
				Boolean shouldPasswordBeStored = false;
				if (answer == JOptionPane.OK_OPTION) {
					password = dialog.getPasswordString();
					shouldPasswordBeStored = dialog.getShouldPasswordBeStored();
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
			appComponent.triggerBambooStatusChecker();

			isDialogShown = false;
		}

	}
}
