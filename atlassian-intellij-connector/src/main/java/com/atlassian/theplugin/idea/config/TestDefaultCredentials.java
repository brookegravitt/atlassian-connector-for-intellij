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

import com.atlassian.connector.commons.jira.JIRAServerFacade2;
import com.atlassian.connector.intellij.bamboo.BambooServerFacade;
import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.TestConnectionProcessor;
import com.atlassian.theplugin.idea.TestConnectionTask;
import com.atlassian.theplugin.idea.config.serverconfig.ProductConnector;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
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

	public static class ServerDataExt {
		public ServerDataExt(final ServerData serverData, final ServerType serverType) {
			this.serverData = serverData;
			this.serverType = serverType;
		}

		private final ServerData serverData;
		private final ServerType serverType;

		public ServerData getServerData() {
			return serverData;
		}

		public ServerType getServerType() {
			return serverType;
		}
	}

	private final Project project;
	private final JComponent parentComponent;
	private final JIRAServerFacade2 jiraServerFacade;
	private final BambooServerFacade bambooServerFacade;
	private Map<ServerData, String> errors = new HashMap<ServerData, String>();

	public TestDefaultCredentials(final Project project, final JComponent parentComponent,
			JIRAServerFacade2 jiraServerFacade, final BambooServerFacade bambooServerFacade) {

		this.project = project;
		this.parentComponent = parentComponent;
		this.jiraServerFacade = jiraServerFacade;
		this.bambooServerFacade = bambooServerFacade;
	}


	public void run(Collection<ServerDataExt> servers) {

		if (servers != null && servers.size() > 0) {
			for (ServerDataExt server : servers) {
				switch (server.getServerType()) {

					case BAMBOO_SERVER:
						testGenericConnection(server.getServerData(), bambooServerFacade);
						break;
					case JIRA_SERVER:
						testGenericConnection(server.getServerData(), jiraServerFacade);
						break;
					default:
						PluginUtil.getLogger().warn("Unknown host type " + server);
				}
			}

			final ArrayList<ServerData> serverDatas = MiscUtil.buildArrayList();
			for (ServerDataExt server : servers) {
				serverDatas.add(server.getServerData());
			}

			final DefaultCredentialsServerList list = new DefaultCredentialsServerList("Default credentials tests result",
					serverDatas);
			ListPopup popup = JBPopupFactory.getInstance().createListPopup(list);
//		popup.showCenteredInCurrentWindow(project); that can cause NPE inside IDEA OpenAPI
			popup.showInCenterOf(parentComponent);
		} else {
			Messages.showInfoMessage(project, "None of servers configuration use default credentials", "Default credentials");
		}
	}


	private boolean testGenericConnection(final ServerData serverData, final ProductServerFacade productServerFacade) {
		TestConnectionTask testConnectionTask = new TestConnectionTask(project, new ProductConnector(productServerFacade),
				serverData, new TestConnectionProcessor() {

			public void setConnectionResult(final ConnectionWrapper.ConnectionState result) {
			}

			public void onSuccess() {
			}

			public void onError(final String errorMessage, Throwable exception, String helpUrl) {
				errors.put(serverData, errorMessage);
			}


		}, "Testing connection : " + serverData.getName(), true, false, false);

		testConnectionTask.setCancelText("Stop");
		ProgressManager.getInstance().run(testConnectionTask);
		return true;
	}


	private class DefaultCredentialsServerList extends BaseListPopupStep<ServerData> {


		public DefaultCredentialsServerList(final String title, final List<ServerData> servers) {
			super(title, servers);
		}

		@Override
		public boolean isSelectable(final ServerData serverCfg) {
			return errors.containsKey(serverCfg);
		}

		@NotNull
		@Override
		public String getTextFor(final ServerData serverCfg) {
			String message = "<html>" + serverCfg.getName();

			if (errors.containsKey(serverCfg)) {
				message += "<font color='red'>&nbsp<i>failed</i></font> ";
			} else {
				message += "<font color='green'>&nbsp<b>ok</b></font>";
			}
			return message + "</html>";
		}

		@Override
		public PopupStep<ServerData> onChosen(final ServerData serverCfg, final boolean b) {
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
