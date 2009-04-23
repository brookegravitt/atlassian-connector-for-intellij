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
package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.TestConnectionListener;
import com.atlassian.theplugin.idea.TestConnectionProcessor;
import com.atlassian.theplugin.idea.TestConnectionTask;
import com.atlassian.theplugin.idea.config.serverconfig.ProductConnector;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

/**
 * User: pmaruszak
 */
public class TestDefaultCredentials {
	private final Project project;
	private final JComponent parentComponent;
	private final JIRAServerFacade jiraServerFacade;
	private final CrucibleServerFacade crucibleServerFacade;
	private final FishEyeServerFacade fishEyeServerFacade;
	private final BambooServerFacade bambooServerFacade;
	private Map<ServerCfg, String> errors = new HashMap<ServerCfg, String>();
	private static CfgManager cfgManager = new AbstractCfgManager() {

		public ServerData getServerData(final Server serverCfg) {
			return new ServerData(serverCfg.getName(), serverCfg.getServerId().toString(), serverCfg.getUserName(),
					serverCfg.getPassword(), serverCfg.getUrl());
		}
	};

	public TestDefaultCredentials(final Project project, final JComponent parentComponent,
			JIRAServerFacade jiraServerFacade, final CrucibleServerFacade crucibleServerFacade,
			final FishEyeServerFacade fishEyeServerFacade, final BambooServerFacade bambooServerFacade) {

		this.project = project;
		this.parentComponent = parentComponent;
		this.jiraServerFacade = jiraServerFacade;
		this.crucibleServerFacade = crucibleServerFacade;
		this.fishEyeServerFacade = fishEyeServerFacade;
		this.bambooServerFacade = bambooServerFacade;
	}


	public void run(Collection<ServerCfg> servers) {

		if (servers != null && servers.size() > 0) {
			for (ServerCfg server : servers) {
				if (server instanceof JiraServerCfg) {
					testGenericConnection(server, jiraServerFacade);
				} else if (server instanceof CrucibleServerCfg) {
					testGenericConnection(server, crucibleServerFacade);
				} else if (server instanceof FishEyeServerCfg) {
					testGenericConnection(server, fishEyeServerFacade);
				} else if (server instanceof BambooServerCfg) {
					testGenericConnection(server, bambooServerFacade);
				} else {
					PluginUtil.getLogger().warn("Unknown host type " + server);
				}
			}

			final DefaultCredentialsServerList list = new DefaultCredentialsServerList("Default credentials tests result",
					new ArrayList(servers));
			ListPopup popup = JBPopupFactory.getInstance().createListPopup(list);
//		popup.showCenteredInCurrentWindow(project); that can cause NPE inside IDEA OpenAPI
			popup.showInCenterOf(parentComponent);
		} else {
			Messages.showInfoMessage(project, "None of servers configuration use default credentials", "Default credentials");
		}
	}


	private boolean testGenericConnection(final ServerCfg serverCfg, final ProductServerFacade productServerFacade) {
		TestConnectionTask testConnectionTask = new TestConnectionTask(project, new ProductConnector(productServerFacade),
				new TestConnectionListener.ServerCfgProvider() {
					public ServerCfg getServer() {
						return serverCfg;
					}
				}, new TestConnectionProcessor() {

			public void setConnectionResult(final ConnectionWrapper.ConnectionState result) {
			}

			public void onSuccess() {
			}

			public void onError(final String errorMessage) {
				errors.put(serverCfg, errorMessage);
			}


		}, "Testing connection : " + serverCfg.getName(), true, false, false);

		testConnectionTask.setCancelText("Stop");
		ProgressManager.getInstance().run(testConnectionTask);
		return true;
	}


	private class DefaultCredentialsServerList extends BaseListPopupStep<ServerCfg> {


		public DefaultCredentialsServerList(final String title, final List<ServerCfg> servers) {
			super(title, servers);
		}

		@Override
		public boolean isSelectable(final ServerCfg serverCfg) {
			return errors.containsKey(serverCfg);
		}

		@NotNull
		@Override
		public String getTextFor(final ServerCfg serverCfg) {
			String message = "<html>" + serverCfg.getName();

			if (errors.containsKey(serverCfg)) {
				message += "<font color='red'>&nbsp<i>failed</i></font> ";
			} else {
				message += "<font color='green'>&nbsp<b>ok</b></font>";
			}
			return message + "</html>";
		}

		@Override
		public PopupStep onChosen(final ServerCfg serverCfg, final boolean b) {
			if (errors.containsKey(serverCfg)) {
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						DialogWithDetails.showExceptionDialog(project, "Connection to " + serverCfg.getName() + " failed",
								errors.get(serverCfg));
					}
				});

			}
			return null;

		}
	}
}
