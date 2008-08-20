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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.util.MiscUtil;
import static com.atlassian.theplugin.commons.util.MiscUtil.buildConcurrentHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class CfgManagerImpl implements CfgManager {
	private final Map<ProjectId, ProjectConfiguration> projectConfigurations = buildConcurrentHashMap(INITIAL_CAPACITY);
    private Collection<ServerCfg> globalServers = MiscUtil.buildArrayList();
	private final Map<ProjectId, Collection<ConfigurationListener>> listeners = buildConcurrentHashMap(100);
	private BambooCfg bambooCfg;
	private static final int INITIAL_CAPACITY = 4;

	private static final ProjectListenerAction PROJECT_UNREGISTERED_LISTENER_ACTION = new ProjectListenerAction() {
		public void run(final ConfigurationListener projectListener, final ProjectId projectId,
				final CfgManagerImpl cfgManager) {
			projectListener.projectUnregistered();
		}
	};
	
	public CfgManagerImpl() {
		// TODO wseliga remove it later on and handle properly null values
		update(new GlobalConfiguration());
	}


	public ProjectConfiguration getProjectConfiguration(final ProjectId projectId) {
		verifyProjectId(projectId);
		return projectConfigurations.get(projectId);
	}

	public Collection<ServerCfg> getAllServers(final ProjectId projectId) {
		if (hasProject(projectId) == false) {
			throw new IllegalArgumentException("Project with id [" + projectId.toString() + "] is not registered.");
		}
        Collection<ServerCfg> tmp = new ArrayList<ServerCfg>(getProjectSpecificServers(projectId));
        tmp.addAll(globalServers);
        return tmp;
    }

    public Collection<ServerCfg> getProjectSpecificServers(final ProjectId projectId) {
		verifyProjectId(projectId);
		ProjectConfiguration res = projectConfigurations.get(projectId);
        if (res == null) {
            return Collections.emptyList();
        }
        return MiscUtil.buildArrayList(res.getServers());
    }

    public Collection<ServerCfg> getGlobalServers() {
        return new ArrayList<ServerCfg>(globalServers);
    }

    public Collection<ServerCfg> getAllEnabledServers(final ProjectId projectId) {
        Collection<ServerCfg> tmp = new ArrayList<ServerCfg>();
        for (ServerCfg serverCfg : getAllServers(projectId)) {
            if (serverCfg.isEnabled()) {
                tmp.add(serverCfg);
            }
        }
        return tmp;
    }

    public BambooCfg getGlobalBambooCfg() {
		return bambooCfg;
    }


	public void updateProjectConfiguration(final ProjectId projectId, final ProjectConfiguration projectConfiguration) {
		verifyProjectId(projectId);
		if (projectConfiguration == null) {
			throw new NullPointerException("Project configuration cannot be null");
		}

		// internalize the list to be private and put it to array
		projectConfigurations.put(projectId, projectConfiguration);
		notifyListeners(projectId, new UpdateConfigurationListenerAction(projectConfiguration));
	}


	private void notifyListeners(final ProjectId projectId, ProjectListenerAction listenerAction) {
		Collection<ConfigurationListener> projectListeners = listeners.get(projectId);
		if (projectListeners != null) {
			for (ConfigurationListener projectListener : projectListeners) {
				listenerAction.run(projectListener, projectId, this);
				//projectListener.updateConfiguration(projectId, this);
			}
		}
	}


	public void updateGlobalConfiguration(final GlobalConfiguration globalConfiguration) {
		if (globalConfiguration == null) {
			throw new NullPointerException("Global configuration cannot be null");
		}
		// internalize the list to be private
		globalServers = MiscUtil.buildArrayList(globalConfiguration.getGlobalServers());
	}

    public void addProjectSpecificServer(final ProjectId projectId, final ServerCfg serverCfg) {
		verifyProjectId(projectId);
		if (serverCfg == null) {
			throw new IllegalArgumentException(ServerCfg.class.getSimpleName() + " cannot be null");
		}

		ProjectConfiguration projectCfg = getProjectConfiguration(projectId);

        if (projectCfg == null) {
            projectCfg = new ProjectConfiguration();
            projectConfigurations.put(projectId, projectCfg);
        }
		if (!projectCfg.getServers().contains(serverCfg)) {
			projectCfg.getServers().add(serverCfg);
		}

	}

    public void addGlobalServer(final ServerCfg serverCfg) {
        globalServers.add(serverCfg);
    }

	public ProjectConfiguration removeProject(final ProjectId projectId) {
		final ProjectConfiguration res = projectConfigurations.remove(projectId);
		if (res != null) {
			notifyListeners(projectId, PROJECT_UNREGISTERED_LISTENER_ACTION);
		}
		return res;
	}

	public ServerCfg removeGlobalServer(final ServerId serverId) {
		verifyServerId(serverId);
		return removeServer(serverId, globalServers);
    }

	private void verifyServerId(final ServerId serverId) {
		if (serverId == null) {
			throw new IllegalArgumentException(ServerId.class.getSimpleName() + " cannot be null");
		}
	}


	public ServerCfg removeProjectSpecificServer(final ProjectId projectId, final ServerId serverId) {
		verifyProjectId(projectId);
		verifyServerId(serverId);

		ProjectConfiguration projectCfg = getProjectConfiguration(projectId);
		if (projectCfg == null) {
			return null;
		}

		return removeServer(serverId, projectCfg.getServers());
	}

	private void verifyProjectId(final ProjectId projectId) {
		if (projectId == null) {
			throw new IllegalArgumentException(ProjectId.class.getSimpleName() + " cannot be null");
		}
	}

	private boolean hasProject(ProjectId projectId) {
		return projectConfigurations.containsKey(projectId);
	}

	public Collection<BambooServerCfg> getAllEnabledBambooServers(final ProjectId projectId) {
		Collection<ServerCfg> tmp = getAllEnabledServers(projectId);
		Collection<BambooServerCfg> res = MiscUtil.buildArrayList();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg instanceof BambooServerCfg) {
				BambooServerCfg bambooServerCfg = (BambooServerCfg) serverCfg;
				res.add(bambooServerCfg);
			}
		}
		return res;
	}

	public ServerCfg getServer(final ProjectId projectId, final ServerId serverId) {
		final Collection<ServerCfg> tmp = getAllServers(projectId);
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerId().equals(serverId)) {
				return serverCfg;
			}
		}
		return null;
	}

	public Collection<CrucibleServerCfg> getAllEnabledCrucibleServers(final ProjectId projectId) {
		Collection<ServerCfg> tmp = getAllEnabledServers(projectId);
		Collection<CrucibleServerCfg> res = MiscUtil.buildArrayList();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg instanceof CrucibleServerCfg) {
				CrucibleServerCfg bambooServerCfg = (CrucibleServerCfg) serverCfg;
				res.add(bambooServerCfg);
			}
		}
		return res;
	}

	public Collection<JiraServerCfg> getAllEnabledJiraServers(final ProjectId projectId) {
		Collection<ServerCfg> tmp = getAllEnabledServers(projectId);
		Collection<JiraServerCfg> res = MiscUtil.buildArrayList();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg instanceof JiraServerCfg) {
				JiraServerCfg bambooServerCfg = (JiraServerCfg) serverCfg;
				res.add(bambooServerCfg);
			}
		}
		return res;
	}


	public Collection<ServerCfg> getAllEnabledServers(final ProjectId projectId, ServerType serverType) {
		Collection<ServerCfg> tmp = getAllEnabledServers(projectId);
		Collection<ServerCfg> res = MiscUtil.buildArrayList();
		for (ServerCfg serverCfg : tmp) {
			if (serverCfg.getServerType() == serverType) {
				res.add(serverCfg);
			}
		}
		return res;
	}


	private ServerCfg removeServer(final ServerId serverId, final Collection<ServerCfg> servers) {
        Iterator<ServerCfg> it = servers.iterator();
        while (it.hasNext()) {
            ServerCfg serverCfg = it.next();
            if (serverCfg.getServerId().equals(serverId)) {
                it.remove();
                return serverCfg;
            }
        }
        return null;
    }

	public void update(GlobalConfiguration globalConfiguration) {
		bambooCfg = globalConfiguration.getBambooCfg();
	}


	public void addProjectConfigurationListener(final ProjectId projectId, final ConfigurationListener configurationListener) {
		if (configurationListener == null) {
			throw new IllegalArgumentException(ProjectId.class.getSimpleName() + " cannot be null");
		}
		verifyProjectId(projectId);

		Collection<ConfigurationListener> tmp = listeners.get(projectId);
		if (tmp == null) {
			tmp = MiscUtil.buildHashSet();
			listeners.put(projectId, tmp);
		}
		tmp.add(configurationListener);
	}

	public boolean removeProjectConfigurationListener(final ProjectId projectId, final ConfigurationListener configurationListener) {
		if (configurationListener == null) {
			throw new IllegalArgumentException(ProjectId.class.getSimpleName() + " cannot be null");
		}
		verifyProjectId(projectId);
		Collection<ConfigurationListener> tmp = listeners.get(projectId);
		return tmp.remove(configurationListener);
	}

	private interface ProjectListenerAction {
		void run(final ConfigurationListener projectListener, final ProjectId projectId, final CfgManagerImpl cfgManager);
	}

	private static class UpdateConfigurationListenerAction implements ProjectListenerAction {

		private final ProjectConfiguration projectConfiguration;

		public UpdateConfigurationListenerAction(final ProjectConfiguration projectConfiguration) {
			this.projectConfiguration = projectConfiguration;
		}

		public void run(final ConfigurationListener projectListener, final ProjectId projectId,
				final CfgManagerImpl cfgManager) {
			projectListener.configurationUpdated(projectConfiguration);
		}
	}
}
