/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.remoteapi;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.idea.PasswordDialog;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

/**
 * Shows a dialog for each Bamboo server that has not the password set.
 */
public class MissingPasswordHandlerJIRA implements Runnable {

	private static boolean isDialogShown = false;

	private final Server server;
	private final ProductServerFacade serverFacade;
	private JIRAToolWindowPanel jiraPanel;

	public MissingPasswordHandlerJIRA(ProductServerFacade serverFacade, Server server, JIRAToolWindowPanel jiraPanel) {
		this.serverFacade = serverFacade;
		this.server = server;
		this.jiraPanel = jiraPanel;
	}

	public void run() {

		if (!isDialogShown) {

			isDialogShown = true;
			boolean wasCanceled = false;

			Server server = ConfigurationFactory.getConfiguration().getProductServers(serverFacade.getServerType()).
					getServer(this.server);

//			if (server.getIsConfigInitialized()) {
//				return; //????
//			}
			PasswordDialog dialog = new PasswordDialog(server, serverFacade);
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
				jiraPanel.selectServer(server);
			} else {
				wasCanceled = true;
			}
			// so or so we assume that user provided password

//			server.setIsConfigInitialized(true);

			if (wasCanceled) {
				Messages.showMessageDialog(
						"You can always change password by changing plugin settings (Preferences | IDE Settings | "
								+ PluginUtil.getName() + ")", "Information", Messages.getInformationIcon());
			}
			isDialogShown = false;
		}

	}
}