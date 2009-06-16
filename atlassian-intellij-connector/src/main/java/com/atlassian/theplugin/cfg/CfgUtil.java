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
package com.atlassian.theplugin.cfg;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.openapi.project.Project;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public final class CfgUtil {

	private static final ProjectId DEFAULT_PROJECT = new ProjectId();

	private CfgUtil() {
		// this is utility class
	}

	public static ProjectId getProjectId(Project project) {
		if (project != null) {
			final String res1 = project.getPresentableUrl();
			if (res1 != null) {
				return new ProjectId(res1);
			}

			final String res2 = project.getName();
			if (res2 != null) {
				return new ProjectId(res2);
			}
		}

		return DEFAULT_PROJECT;
	}

	public static JiraServerCfg getJiraServerCfgbyServerId(final ProjectCfgManagerImpl projectCfgManager,
			final String serverId) {
		for (JiraServerCfg server : projectCfgManager.getAllEnabledJiraServers()) {
			if (server.getServerId().toString().equals(serverId)) {
				return server;

			}
		}
		return null;
	}

	public static ServerCfg getEnabledServerCfgbyServerId(
			final ProjectCfgManagerImpl projectCfgManager, final String serverId) {
		for (ServerCfg server : projectCfgManager.getAllEnabledServers()) {
			if (server.getServerId().toString().equals(serverId)) {
				return server;

			}
		}
		return null;
	}

	public static JiraServerCfg getJiraServerCfgByUrl(final ProjectCfgManagerImpl projectCfgManager,
			final String serverUrl) {
		for (JiraServerCfg server : projectCfgManager.getAllEnabledJiraServers()) {
			if (server.getUrl().equals(serverUrl)) {
				return server;

			}
		}
		return null;
	}

	/**
	 * Finds server with specified url in collection of servers.
	 * It tries to find enabled server. If not found then tries to find disabled server.
	 * It compares host, port and path (skips protocol and query string)
	 *
	 * @param serverUrl url of server
	 * @param servers   collection of servers
	 * @param cfg	   project configuration
	 * @return ServerData or null if not found
	 */
	public static ServerData findServer(final URL serverUrl, final Collection<ServerCfg> servers, final ProjectCfgManager cfg) {

		ServerData enabledServer = null;
		ServerData disabledServer = null;

		// find matching server
		for (ServerCfg server : servers) {

			URL url;

			try {
				url = new URL(server.getUrl());
			} catch (MalformedURLException e) {
				// skip the server if url is broken
				continue;
			}

			// compare urls (skip protocols and query string)
			if (url.getHost().equalsIgnoreCase(serverUrl.getHost())
					&& url.getPort() == serverUrl.getPort()
					&& (((url.getPath() == null || url.getPath().equals("") || url.getPath().equals("/"))
					&& (serverUrl.getPath() == null || serverUrl.getPath().equals("") || serverUrl.getPath().equals("/")))
					|| (url.getPath() != null && serverUrl.getPath() != null && url.getPath().equals(serverUrl.getPath())))) {

				if (server.isEnabled()) {
					enabledServer = cfg.getServerData(server);
					break;
				} else if (disabledServer == null) {
					disabledServer = cfg.getServerData(server);
				}
			}
		}

		if (enabledServer != null) {
			return enabledServer;
		}

		return disabledServer;
	}

	/**
	 * Finds server with specified url in collection of servers (exact String match).
	 * It tries to find enabled server. If not found then tries to find disabled server.
	 *
	 * @param serverUrl url of server
	 * @param servers   collection of servers
	 * @param cfg	   project configuration
	 * @return ServerData or null if not found
	 */
	public static ServerData findServer(final String serverUrl, final Collection<ServerCfg> servers,
			final ProjectCfgManager cfg) {

		ServerData enabledServer = null;
		ServerData disabledServer = null;

		// find matching server
		for (ServerCfg server : servers) {
			if (server.getUrl().equals(serverUrl)) {
				if (server.isEnabled()) {
					enabledServer = cfg.getServerData(server);
					break;
				} else if (disabledServer == null) {
					disabledServer = cfg.getServerData(server);
				}
			}
		}

		if (enabledServer != null) {
			return enabledServer;
		}

		return disabledServer;
	}
}
