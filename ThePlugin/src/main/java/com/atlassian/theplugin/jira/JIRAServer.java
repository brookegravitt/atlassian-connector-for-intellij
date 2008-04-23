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

package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.remoteapi.RemoteApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JIRAServer {
	private static final int LIST_SPECIAL_VALUES_COUNT = 1;
	private static final int VERSION_SPECIAL_VALUES_COUNT = 4;
	private static final int COMPONENTS_SPECIAL_VALUES_COUNT = 2;
	public static final int ANY_ID = -1000;
	private static final int NO_VERSION_ID = -1;
	private static final int RELEASED_VERSION_ID = -3;
	private static final int UNRELEASED_VERSION_ID = -2;
	private static final int NO_COMPONENT_ID = -1;
	private static final int UNRESOLVED_ID = -1;

	private Server server;
	private boolean validServer = false;
	private String errorMessage = null;

	private List<JIRAProject> projects;
	private List statuses;
	private List issueTypes;
	private List savedFilters;
	private List<JIRAVersionBean> serverVersions;
	private List<JIRAVersionBean> versions;
	private List<JIRAFixForVersionBean> fixForVersions;
	private List components;
	private List priorieties;
	private List resolutions;

	private JIRAProject lastProject = null;
	private JIRAProject currentProject = null;

	private final JIRAServerFacade jiraServerFacade;


	public JIRAServer(JIRAServerFacade jiraServerFacade) {
		this.jiraServerFacade = jiraServerFacade;
	}

	public JIRAServer(Server server, JIRAServerFacade jiraServerFacade) {
		this.server = server;
		this.jiraServerFacade = jiraServerFacade;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public boolean checkServer() {
		try {
			jiraServerFacade.testServerConnection(server.getUrlString(), server.getUserName(), server.getPasswordString());
			validServer = true;
		} catch (RemoteApiException e) {
			errorMessage = e.getMessage();
			PluginUtil.getLogger().error(errorMessage);
		}
		return validServer;
	}

	public List<JIRAProject> getProjects() {
		if (projects == null) {
			try {
				List<JIRAProject> retrieved = jiraServerFacade.getProjects(server);
				projects = new ArrayList<JIRAProject>();
				projects.add(new JIRAProjectBean(ANY_ID, "Any"));
				projects.addAll(retrieved);
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				projects = Collections.EMPTY_LIST;
			}
		}
		return projects;
	}

	public List<JIRAConstant> getStatuses() {
		if (statuses == null) {
			try {
				List<JIRAConstant> retrieved = jiraServerFacade.getStatuses(server);
				statuses = new ArrayList<JIRAConstant>(retrieved.size() + 1);
				statuses.add(new JIRAStatusBean(ANY_ID, "Any", null));
				statuses.addAll(retrieved);
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				statuses = Collections.EMPTY_LIST;
			}
		}

		return statuses;
	}

	public List<JIRAConstant> getIssueTypes() {
		if (issueTypes == null) {
			try {
				List<JIRAConstant> retrieved;
				if (currentProject == null) {
					retrieved = jiraServerFacade.getIssueTypes(server);
				} else {
					retrieved = jiraServerFacade.getIssueTypesForProject(server, Long.toString(currentProject.getId()));
				}
				issueTypes = new ArrayList<JIRAConstant>(retrieved.size() + 1);
				issueTypes.add(new JIRAIssueTypeBean(ANY_ID, "Any", null));
				issueTypes.addAll(retrieved);

				lastProject = currentProject;
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				issueTypes = Collections.EMPTY_LIST;
			}
		}

		return issueTypes;
	}

	public List<JIRAConstant> getSavedFilters() {
		if (savedFilters == null) {
			try {
				savedFilters = jiraServerFacade.getSavedFilters(server);
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				savedFilters = Collections.EMPTY_LIST;
			}
		}
		return savedFilters;
	}

	public List<JIRAConstant> getPriorieties() {
		if (priorieties == null) {
			try {
				List<JIRAConstant> retrieved = jiraServerFacade.getPriorieties(server);
				priorieties = new ArrayList(retrieved.size() + 1);
				priorieties.add(new JIRAPriorityBean(ANY_ID, "Any", null));
				priorieties.addAll(retrieved);
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				priorieties = Collections.EMPTY_LIST;
			}
		}
		return priorieties;
	}

	public List<JIRAConstant> getResolutions() {
		if (resolutions == null) {
			try {
				List<JIRAQueryFragment> retrieved = jiraServerFacade.getResolutions(server);
				resolutions = new ArrayList<JIRAResolutionBean>(retrieved.size() + 1);
				resolutions.add(new JIRAResolutionBean(ANY_ID, "Any"));
				resolutions.add(new JIRAResolutionBean(UNRESOLVED_ID, "Unresolved"));
				resolutions.addAll(retrieved);
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				resolutions = Collections.EMPTY_LIST;
			}
		}
		return resolutions;
	}

	private List<JIRAVersionBean> getAllVersions() {
		if (serverVersions == null) {
			try {
				if (currentProject != null) {
					serverVersions = jiraServerFacade.getVersions(server, currentProject.getKey());
					lastProject = currentProject;
				} else {
					serverVersions = Collections.EMPTY_LIST;
				}

			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				serverVersions = Collections.EMPTY_LIST;
			}
		}
		return serverVersions;
	}

	public List<JIRAVersionBean> getVersions() {
		if (versions == null) {
			errorMessage = null;
			if (currentProject != null) {
				List<JIRAVersionBean> retrieved = getAllVersions();
				if (!retrieved.isEmpty()) {
					versions = new ArrayList<JIRAVersionBean>(retrieved.size() + VERSION_SPECIAL_VALUES_COUNT);
					versions.add(new JIRAVersionBean(ANY_ID, "Any"));
					versions.add(new JIRAVersionBean(NO_VERSION_ID, "No version"));
					versions.add(new JIRAVersionBean(RELEASED_VERSION_ID, "Released versions"));
					for (JIRAQueryFragment jiraQueryFragment : retrieved) {
						if (((JIRAVersionBean) jiraQueryFragment).isReleased()) {
							versions.add((JIRAVersionBean) jiraQueryFragment);
						}
					}
					versions.add(new JIRAVersionBean(UNRELEASED_VERSION_ID, "Unreleased versions"));
					for (JIRAQueryFragment jiraQueryFragment : retrieved) {
						if (!((JIRAVersionBean) jiraQueryFragment).isReleased()) {
							versions.add((JIRAVersionBean) jiraQueryFragment);
						}
					}
				} else {
					versions = Collections.EMPTY_LIST;
				}
				lastProject = currentProject;
			} else {
				versions = Collections.EMPTY_LIST;
			}
		}

		return versions;
	}

	public List<JIRAFixForVersionBean> getFixForVersions() {
		if (fixForVersions == null) {
			if (currentProject != null) {
				List<JIRAVersionBean> retrieved = getAllVersions();
				if (!retrieved.isEmpty()) {
					fixForVersions = new ArrayList<JIRAFixForVersionBean>(retrieved.size() + VERSION_SPECIAL_VALUES_COUNT);
					fixForVersions.add(new JIRAFixForVersionBean(ANY_ID, "Any"));
					fixForVersions.add(new JIRAFixForVersionBean(NO_VERSION_ID, "No version"));
					fixForVersions.add(new JIRAFixForVersionBean(RELEASED_VERSION_ID, "Released versions"));
					for (JIRAVersionBean jiraQueryFragment : retrieved) {
						if (jiraQueryFragment.isReleased()) {
							fixForVersions.add(new JIRAFixForVersionBean(jiraQueryFragment));
						}
					}
					fixForVersions.add(new JIRAFixForVersionBean(UNRELEASED_VERSION_ID, "Unreleased versions"));
					for (JIRAVersionBean jiraQueryFragment : retrieved) {
						if (!jiraQueryFragment.isReleased()) {
							fixForVersions.add(new JIRAFixForVersionBean(jiraQueryFragment));
						}
					}
				} else {
					fixForVersions = Collections.EMPTY_LIST;
				}
				lastProject = currentProject;
			} else {
				fixForVersions = Collections.EMPTY_LIST;
			}
		}
		return fixForVersions;
	}

	public List<JIRAQueryFragment> getComponents() {
		if (components == null || (currentProject != null && !currentProject.getKey().equals(lastProject.getKey()))) {
			try {
				if (currentProject != null) {
					List<JIRAQueryFragment> retrieved = jiraServerFacade.getComponents(server, currentProject.getKey());
					components = new ArrayList<JIRAVersionBean>(retrieved.size() + 1);
					components.add(new JIRAComponentBean(ANY_ID, "Any"));
					components.add(new JIRAComponentBean(NO_COMPONENT_ID, "No component"));
					components.addAll(retrieved);
					lastProject = currentProject;
				} else {
					components = Collections.EMPTY_LIST;
				}
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				components = Collections.EMPTY_LIST;
			}
		}

		return components;
	}

	public boolean isValidServer() {
		return validServer;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public JIRAProject getCurrentProject() {
		return currentProject;
	}

	public void setCurrentProject(JIRAProject currentProject) {
		this.currentProject = currentProject;
		versions = null;
		fixForVersions = null;
		components = null;
		issueTypes = null;
	}
}
