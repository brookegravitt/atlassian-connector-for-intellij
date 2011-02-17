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

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.idea.PasswordDialog;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

/**
 * Shows a dialog for each JIRA server that has not the password set.
 */
public class MissingPasswordHandlerJIRA implements MissingPasswordHandlerQueue.Handler {

	private static boolean isDialogShown = false;

	private final JiraServerCfg server;
	private Project project;
	private final ProductServerFacade serverFacade;

	public MissingPasswordHandlerJIRA(ProductServerFacade serverFacade, JiraServerCfg server, final Project project) {
		this.serverFacade = serverFacade;
		this.server = server;
		this.project = project;
	}

	public void go() {

		if (!isDialogShown) {

			isDialogShown = true;
			boolean wasCanceled = false;

//			if (server.getIsConfigInitialized()) {
//				return; //????
//			}
			PasswordDialog dialog = new PasswordDialog(server, serverFacade, project);
			dialog.pack();
			JPanel panel = dialog.getPasswordPanel();
			int answer = JOptionPane.CANCEL_OPTION;

			if (server.isUseDefaultCredentials()) {
				Messages.showInfoMessage(project,
						"Either do not use default credentials for " + server.getName() + " or change default credentials",
						PluginUtil.PRODUCT_NAME);
			} else {
				answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), panel,
						PluginUtil.PRODUCT_NAME, OK_CANCEL_OPTION, PLAIN_MESSAGE);
			}

			if (!server.isUseDefaultCredentials() && answer == JOptionPane.OK_OPTION) {
				String password = dialog.getPasswordString();
				Boolean shouldPasswordBeStored = dialog.getShouldPasswordBeStored();
				server.setPassword(password);
				server.setPasswordStored(shouldPasswordBeStored);
				server.setUsername(dialog.getUserName());
			} else {
				wasCanceled = true;
			}
			// so or so we assume that user provided password

//			server.transientSetIsConfigInitialized(true);

			if (wasCanceled && !server.isUseDefaultCredentials()) {
				Messages.showMessageDialog(
						"You can always change password by changing plugin settings (Preferences | IDE Settings | "
								+ PluginUtil.getInstance().getName() + ")", "Information", Messages.getInformationIcon());
			}
			isDialogShown = false;
		}

	}
}