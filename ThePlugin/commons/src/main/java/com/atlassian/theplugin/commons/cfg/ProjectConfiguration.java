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
package com.atlassian.theplugin.commons.cfg;

import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.Collection;
import java.util.ArrayList;

public class ProjectConfiguration {
	private Collection<ServerCfg> servers;

	public ProjectConfiguration(final ProjectConfiguration other) {
		servers = cloneArrayList(other.getServers());
	}

	public static Collection<ServerCfg> cloneArrayList(final Collection<ServerCfg> collection) {
		final ArrayList<ServerCfg> res = new ArrayList<ServerCfg>(collection.size());
		for (ServerCfg serverCfg : collection) {
			res.add(serverCfg.getClone());
		}
		return res;
	}


	public ProjectConfiguration(final Collection<ServerCfg> servers) {
		if (servers == null) {
			throw new NullPointerException("Servers cannot be null");
		}
		this.servers = servers;
	}

	public ProjectConfiguration() {
		this.servers = MiscUtil.buildArrayList();
	}

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ProjectConfiguration)) {
			return false;
		}

		final ProjectConfiguration that = (ProjectConfiguration) o;

		if (!servers.equals(that.servers)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return servers.hashCode();
	}

	public Collection<ServerCfg> getServers() {
		return servers;
	}

	public ServerCfg getServerCfg(ServerId serverId) {
		for (ServerCfg serverCfg : servers) {
			if (serverId.equals(serverCfg.getServerId())) {
				return serverCfg;
			}
		}
		return null;
	}

	public static ProjectConfiguration emptyConfiguration() {
		return new ProjectConfiguration();
	}

	public ProjectConfiguration getClone() {
		return new ProjectConfiguration(this);
	}
}
