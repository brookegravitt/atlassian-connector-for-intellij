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

package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.*;

public class JIRAServerCache {
	private static final int VERSION_SPECIAL_VALUES_COUNT = 4;
	public static final int ANY_ID = -1000;
	private static final int NO_VERSION_ID = -1;
	private static final int RELEASED_VERSION_ID = -3;
	private static final int UNRELEASED_VERSION_ID = -2;
	public static final int UNKNOWN_COMPONENT_ID = -1;
	private static final int UNRESOLVED_ID = -1;

	private final ServerData server;
	private boolean validServer;
	private String errorMessage;

	private List<JIRAProject> projects;
	private List<JIRAConstant> statuses;

	private List<JIRAQueryFragment> savedFilters;
	private List<JIRAPriorityBean> priorities;
	private List<JIRAResolutionBean> resolutions;
	private List<JIRAConstant> globalIssueTypes;
	private List<JIRAConstant> globalSubtaskIssueTypes;

	private Map<String, List<JIRAConstant>> issueTypesCache;
	private Map<String, List<JIRAConstant>> subtaskIssueTypesCache;
	private Map<String, List<JIRAVersionBean>> serverVersionsCache;
	private Map<String, List<JIRAComponentBean>> componentsCache;

	private final JIRAServerFacade jiraServerFacade;

	public JIRAServerCache(ServerData server, JIRAServerFacade jiraServerFacade) {
		this.jiraServerFacade = jiraServerFacade;
		this.issueTypesCache = new HashMap<String, List<JIRAConstant>>();
		this.subtaskIssueTypesCache = new HashMap<String, List<JIRAConstant>>();
		this.serverVersionsCache = new HashMap<String, List<JIRAVersionBean>>();
		this.componentsCache = new HashMap<String, List<JIRAComponentBean>>();
		this.server = server;
	}

	public ServerData getServer() {
		return server;
	}

	public boolean checkServer() throws RemoteApiException {
		try {
			jiraServerFacade.testServerConnection(server);
			validServer = true;

		} catch (RemoteApiLoginException e) {
			errorMessage = e.getMessage();
			PluginUtil.getLogger().error(errorMessage);
			validServer = false;

		} catch (RemoteApiException e) {
			errorMessage = e.getMessage();
			PluginUtil.getLogger().error(errorMessage);
			validServer = false;
			throw e;
		}
		return validServer;
	}

	public List<JIRAProject> getProjects() throws JIRAException {
		if (projects == null) {
			try {
				List<JIRAProject> retrieved = jiraServerFacade.getProjects(server);
				projects = new ArrayList<JIRAProject>();
				projects.add(new JIRAProjectBean(ANY_ID, "Any"));
				projects.addAll(retrieved);
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				throw e;
			}
		}
		return projects;
	}

	public List<JIRAConstant> getStatuses() throws JIRAException {
		if (statuses == null) {
			try {
				List<JIRAConstant> retrieved = jiraServerFacade.getStatuses(server);
				statuses = new ArrayList<JIRAConstant>(retrieved.size() + 1);
				statuses.add(new JIRAStatusBean(ANY_ID, "Any", null));
				statuses.addAll(retrieved);
				for (JIRAConstant status : statuses) {
					CachedIconLoader.getIcon(status.getIconUrl());
				}
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				throw e;
			}
		}

		return statuses;
	}

	/**
	 * @param project
	 * @param includeAny
	 * @param includeAny
	 * @return list of issue types or empty collection
	 * @throws com.atlassian.theplugin.jira.api.JIRAException
	 *
	 */
	public List<JIRAConstant> getIssueTypes(JIRAProject project, boolean includeAny) throws JIRAException {
		List<JIRAConstant> issueTypes = project == null ? globalIssueTypes : issueTypesCache.get(project.getKey());

		if (issueTypes == null) {
			List<JIRAConstant> retrieved;
			try {
				if (project == null || project.getKey() == null) {
					retrieved = jiraServerFacade.getIssueTypes(server);
				} else {
					retrieved = jiraServerFacade.getIssueTypesForProject(server, Long.toString(project.getId()));
				}
				issueTypes = new ArrayList<JIRAConstant>(retrieved.size());
				issueTypes.addAll(retrieved);

				for (JIRAConstant issueType : issueTypes) {
					CachedIconLoader.getIcon(issueType.getIconUrl());
				}

				if (project != null) {
					issueTypesCache.put(project.getKey(), issueTypes);
				} else {
					globalIssueTypes = issueTypes;
				}
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				if (globalIssueTypes != null) {
					issueTypes = globalIssueTypes;
				} else {
					throw e;
				}
			}
		}

		List<JIRAConstant> result = new ArrayList<JIRAConstant>();
		if (includeAny) {
			result.add(new JIRAIssueTypeBean(ANY_ID, "Any", null));
		}
		result.addAll(issueTypes);

		return result;
	}

	public List<JIRAConstant> getSubtaskIssueTypes(JIRAProject project) throws JIRAException {
		List<JIRAConstant> subtaskTypes =
				project == null ? globalSubtaskIssueTypes : subtaskIssueTypesCache.get(project.getKey());

		if (subtaskTypes == null) {
			List<JIRAConstant> retrieved;
			try {
				if (project == null || project.getKey() == null) {
					retrieved = jiraServerFacade.getSubtaskIssueTypes(server);
				} else {
					retrieved = jiraServerFacade.getSubtaskIssueTypesForProject(server, Long.toString(project.getId()));
				}
				subtaskTypes = new ArrayList<JIRAConstant>(retrieved.size());
				subtaskTypes.addAll(retrieved);

				for (JIRAConstant subtaskType : subtaskTypes) {
					CachedIconLoader.getIcon(subtaskType.getIconUrl());
				}

				if (project != null) {
					subtaskIssueTypesCache.put(project.getKey(), subtaskTypes);
				} else {
					globalSubtaskIssueTypes = subtaskTypes;
				}
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				if (globalSubtaskIssueTypes != null) {
					subtaskTypes = globalSubtaskIssueTypes;
				} else {
					throw e;
				}
			}
		}

		return subtaskTypes;
	}

	public List<JIRAQueryFragment> getSavedFilters() throws JIRAException {
		if (savedFilters == null) {
			try {
				savedFilters = jiraServerFacade.getSavedFilters(server);
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				throw e;
			}
		}
		return savedFilters;
	}

	public List<JIRAPriorityBean> getPriorities(boolean includeAny) throws JIRAException {
		if (priorities == null) {
			try {
				List<JIRAPriorityBean> retrieved = jiraServerFacade.getPriorities(server);
				priorities = new ArrayList<JIRAPriorityBean>(retrieved.size() + 1);
				priorities.addAll(retrieved);
				for (JIRAConstant priority : priorities) {
					CachedIconLoader.getIcon(priority.getIconUrl());
				}
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				throw e;
			}
		}

		List<JIRAPriorityBean> result = new ArrayList<JIRAPriorityBean>();
		if (includeAny) {
			result.add(new JIRAPriorityBean(ANY_ID, -1, "Any", null));
		}
		result.addAll(priorities);
		return result;
	}

	public List<JIRAResolutionBean> getResolutions(boolean includeAnyAndUnknown) throws JIRAException {
		if (resolutions == null) {
			try {
				List<JIRAResolutionBean> retrieved = jiraServerFacade.getResolutions(server);
				resolutions = new ArrayList<JIRAResolutionBean>(retrieved.size());
				resolutions.addAll(retrieved);
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				throw e;
			}
		}
		List<JIRAResolutionBean> result;
		result = new ArrayList<JIRAResolutionBean>();
		if (includeAnyAndUnknown) {
			result.add(new JIRAResolutionBean(ANY_ID, "Any"));
			result.add(new JIRAResolutionBean(UNRESOLVED_ID, "Unresolved"));
		}
		result.addAll(resolutions);

		return result;
	}

	private List<JIRAVersionBean> getAllVersions(JIRAProject project) throws JIRAException {
		List<JIRAVersionBean> versions = null;
		if (project != null) {
			versions = serverVersionsCache.get(project.getKey());
		}
		if (versions == null) {
			try {
				if (project != null && project.getKey() != null) {
					versions = jiraServerFacade.getVersions(server, project.getKey());
					serverVersionsCache.put(project.getKey(), versions);
				} else {
					versions = Collections.emptyList();
				}

			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				throw e;
			}
		}
		return versions;
	}

	public List<JIRAVersionBean> getVersions(JIRAProject project, boolean includeSpecialValues) throws JIRAException {
		List<JIRAVersionBean> versions;
		if (project != null) {
			List<JIRAVersionBean> retrieved = getAllVersions(project);
			if (!retrieved.isEmpty()) {
				versions = new ArrayList<JIRAVersionBean>(retrieved.size() + VERSION_SPECIAL_VALUES_COUNT);
				if (includeSpecialValues) {
					versions.add(new JIRAVersionBean(ANY_ID, "Any"));
					versions.add(new JIRAVersionBean(NO_VERSION_ID, "No version"));
					versions.add(new JIRAVersionBean(RELEASED_VERSION_ID, "Released versions"));
				}
				for (JIRAQueryFragment jiraQueryFragment : retrieved) {
					if (((JIRAVersionBean) jiraQueryFragment).isReleased()) {
						versions.add((JIRAVersionBean) jiraQueryFragment);
					}
				}
				if (includeSpecialValues) {
					versions.add(new JIRAVersionBean(UNRELEASED_VERSION_ID, "Unreleased versions"));
				}
				for (JIRAQueryFragment jiraQueryFragment : retrieved) {
					if (!((JIRAVersionBean) jiraQueryFragment).isReleased()) {
						versions.add((JIRAVersionBean) jiraQueryFragment);
					}
				}
			} else {
				versions = Collections.emptyList();
			}
		} else {
			versions = Collections.emptyList();
		}

		return versions;
	}

	public List<JIRAFixForVersionBean> getFixForVersions(JIRAProject project, boolean includeSpecialValues)
			throws JIRAException {
		List<JIRAFixForVersionBean> fixForVersions;
		if (project != null) {
			List<JIRAVersionBean> retrieved = getAllVersions(project);
			if (!retrieved.isEmpty()) {
				fixForVersions = new ArrayList<JIRAFixForVersionBean>(retrieved.size() + VERSION_SPECIAL_VALUES_COUNT);
				if (includeSpecialValues) {
					fixForVersions.add(new JIRAFixForVersionBean(ANY_ID, "Any"));
					fixForVersions.add(new JIRAFixForVersionBean(NO_VERSION_ID, "No version"));
					fixForVersions.add(new JIRAFixForVersionBean(RELEASED_VERSION_ID, "Released versions"));
				}
				for (JIRAVersionBean jiraQueryFragment : retrieved) {
					if (jiraQueryFragment.isReleased()) {
						fixForVersions.add(new JIRAFixForVersionBean(jiraQueryFragment));
					}
				}

				if (includeSpecialValues) {
					fixForVersions.add(new JIRAFixForVersionBean(UNRELEASED_VERSION_ID, "Unreleased versions"));
				}

				for (JIRAVersionBean jiraQueryFragment : retrieved) {
					if (!jiraQueryFragment.isReleased()) {
						fixForVersions.add(new JIRAFixForVersionBean(jiraQueryFragment));
					}
				}
			} else {
				fixForVersions = Collections.emptyList();
			}
		} else {
			fixForVersions = Collections.emptyList();
		}
		return fixForVersions;
	}

	public List<JIRAComponentBean> getComponents(JIRAProject project, final boolean includeSpecialValues) throws JIRAException {
		List<JIRAComponentBean> components = null;
		if (project != null) {
			components = componentsCache.get(project.getKey());
		}
		if (components == null) {
			try {
				if (project != null && project.getKey() != null) {
					List<JIRAComponentBean> retrieved = jiraServerFacade.getComponents(server, project.getKey());

					components = new ArrayList<JIRAComponentBean>(retrieved.size() + 1);
					if (includeSpecialValues) {
						components.add(new JIRAComponentBean(ANY_ID, "Any"));
						components.add(new JIRAComponentBean(UNKNOWN_COMPONENT_ID, "Unknown"));
					}
					components.addAll(retrieved);

					componentsCache.put(project.getKey(), components);
				} else {
					components = Collections.emptyList();
				}
			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				throw e;
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
}
