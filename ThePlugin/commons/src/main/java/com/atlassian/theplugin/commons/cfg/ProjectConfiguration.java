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

import java.util.ArrayList;
import java.util.Collection;

public class ProjectConfiguration {
	private Collection<ServerCfg> servers;

	private ServerId defaultCrucibleServer;
	private ServerId defaultFishEyeServer;
	private String defaultCrucibleProject;
	private String defaultCrucibleRepo;
	private String fishEyeProjectPath;
	private static final int HASHCODE_MAGIC = 31;


	public ProjectConfiguration(final ProjectConfiguration other) {
		servers = cloneArrayList(other.getServers());
		defaultCrucibleServer = other.defaultCrucibleServer;
		defaultFishEyeServer = other.defaultFishEyeServer;
		defaultCrucibleProject = other.defaultCrucibleProject;
		defaultCrucibleRepo = other.defaultCrucibleRepo;
		fishEyeProjectPath = other.fishEyeProjectPath;
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

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ProjectConfiguration)) {
			return false;
		}

		final ProjectConfiguration that = (ProjectConfiguration) o;

		if (defaultCrucibleProject != null ? !defaultCrucibleProject.equals(that.defaultCrucibleProject) :
				that.defaultCrucibleProject != null) {
			return false;
		}
		if (defaultCrucibleServer != null ? !defaultCrucibleServer.equals(that.defaultCrucibleServer) :
				that.defaultCrucibleServer != null) {
			return false;
		}
		if (defaultCrucibleRepo != null ? !defaultCrucibleRepo.equals(that.defaultCrucibleRepo) :
				that.defaultCrucibleRepo != null) {
			return false;
		}
		if (defaultFishEyeServer != null ? !defaultFishEyeServer.equals(that.defaultFishEyeServer) :
				that.defaultFishEyeServer != null) {
			return false;
		}
		if (fishEyeProjectPath != null ? !fishEyeProjectPath.equals(that.fishEyeProjectPath) :
				that.fishEyeProjectPath != null) {
			return false;
		}
		if (!servers.equals(that.servers)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = servers.hashCode();
		result = HASHCODE_MAGIC * result + (defaultCrucibleServer != null ? defaultCrucibleServer.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultFishEyeServer != null ? defaultFishEyeServer.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultCrucibleProject != null ? defaultCrucibleProject.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultCrucibleRepo != null ? defaultCrucibleRepo.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (fishEyeProjectPath != null ? fishEyeProjectPath.hashCode() : 0);
		return result;
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

	public ServerId getDefaultCrucibleServer() {
		return defaultCrucibleServer;
	}

	public void setDefaultCrucibleServer(final ServerId defaultCrucibleServerId) {
		this.defaultCrucibleServer = defaultCrucibleServerId;
	}

	public ServerId getDefaultFishEyeServer() {
		return defaultFishEyeServer;
	}

	public void setDefaultFishEyeServer(final ServerId defaultFishEyeServerId) {
		this.defaultFishEyeServer = defaultFishEyeServerId;
	}

	public String getDefaultCrucibleProject() {
		return defaultCrucibleProject;
	}

	public void setDefaultCrucibleProject(final String defaultCrucibleProject) {
		this.defaultCrucibleProject = defaultCrucibleProject;
	}

	public String getDefaultCrucibleRepo() {
		return defaultCrucibleRepo;
	}

	public void setDefaultCrucibleRepo(final String defaultFishEyeRepo) {
		this.defaultCrucibleRepo = defaultFishEyeRepo;
	}

	public String getFishEyeProjectPath() {
		return fishEyeProjectPath;
	}

	public void setFishEyeProjectPath(final String fishEyeProjectPath) {
		this.fishEyeProjectPath = fishEyeProjectPath;
	}
}
