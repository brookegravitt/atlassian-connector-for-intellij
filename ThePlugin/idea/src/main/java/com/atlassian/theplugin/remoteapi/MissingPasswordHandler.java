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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.ConfigurationListener;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PasswordDialog;
import com.atlassian.theplugin.idea.ThePluginApplicationComponent;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import java.util.Set;

/**
 * Shows a dialog for each Bamboo server that has not the password set.
 */
public class MissingPasswordHandler implements Runnable, ConfigurationListener {

	private static boolean isDialogShown = false;

	private final ProductServerFacade serverFacade;
	private final CfgManager cfgManager;
	private final Project project;
	private final Set<ServerId> serversWithoutPassword = MiscUtil.buildHashSet();
	private boolean shouldStop;


	public MissingPasswordHandler(ProductServerFacade serverFacade, final CfgManager cfgManager, final Project project) {
		this.serverFacade = serverFacade;
		this.cfgManager = cfgManager;
		this.project = project;
		cfgManager.addListener(CfgUtil.getProjectId(project), this);
	}

	private synchronized boolean shouldStop() {
		return shouldStop;
	}

	public void run() {

		if (!isDialogShown && !shouldStop()) {

			isDialogShown = true;
			boolean wasCanceled = false;

			for (ServerCfg server : cfgManager.getAllEnabledServers(
					CfgUtil.getProjectId(project), serverFacade.getServerType())) {
				if (server.isComplete() || serversWithoutPassword.contains(server.getServerId())) {
					continue;
				}
				PasswordDialog dialog = new PasswordDialog(server, serverFacade);
				dialog.pack();
				JPanel panel = dialog.getPasswordPanel();

				int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), panel,
						PluginUtil.getInstance().getName(), OK_CANCEL_OPTION, PLAIN_MESSAGE);

				if (answer == JOptionPane.OK_OPTION) {
					String password = dialog.getPasswordString();
					Boolean shouldPasswordBeStored = dialog.getShouldPasswordBeStored();
					server.setPassword(password);
					server.setPasswordStored(shouldPasswordBeStored);
					server.setUsername(dialog.getUserName());
				} else {
					wasCanceled = true;
					serversWithoutPassword.add(server.getServerId());
				}
			}
			ThePluginApplicationComponent appComponent = IdeaHelper.getAppComponent();
			appComponent.rescheduleStatusCheckers(true);

			if (wasCanceled) {
				Messages.showMessageDialog(
						"You can always change password by changing plugin settings (Preferences | IDE Settings | "
								+ PluginUtil.getInstance().getName() + ")", "Information", Messages.getInformationIcon());
			}
			isDialogShown = false;
		}

	}

	public void updateConfiguration(final ProjectId project, final CfgManager cfgManager) {
	}

	public synchronized void projectUnregistered() {
		shouldStop = true;
	}
}
