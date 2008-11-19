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

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.*;

public class JIRAServerCache {
    private static final int VERSION_SPECIAL_VALUES_COUNT = 4;
    public static final int ANY_ID = -1000;
    private static final int NO_VERSION_ID = -1;
    private static final int RELEASED_VERSION_ID = -3;
    private static final int UNRELEASED_VERSION_ID = -2;
    private static final int NO_COMPONENT_ID = -1;
    private static final int UNRESOLVED_ID = -1;

    private final JiraServerCfg server;
    private boolean validServer = false;
    private String errorMessage = null;

    private List<JIRAProject> projects;
    private List<JIRAConstant> statuses;

    private List<JIRAQueryFragment> savedFilters;
    private List<JIRAConstant> priorities;
    private List<JIRAResolutionBean> resolutions;
	private List<JIRAConstant> globalIssueTypes;

	private Map<String, List<JIRAConstant>> issueTypesCache;
    private Map<String, List<JIRAVersionBean>> serverVersionsCache;
    private Map<String, List<JIRAComponentBean>> componentsCache;

	private final JIRAServerFacade jiraServerFacade;

    public JIRAServerCache(JiraServerCfg server, JIRAServerFacade jiraServerFacade) {
		this.jiraServerFacade = jiraServerFacade;
		this.issueTypesCache = new HashMap<String, List<JIRAConstant>>();
		this.serverVersionsCache = new HashMap<String, List<JIRAVersionBean>>();
		this.componentsCache = new HashMap<String, List<JIRAComponentBean>>();
        this.server = server;
    }

    public JiraServerCfg getServer() {
        return server;
    }

    public boolean checkServer() {
        try {
            jiraServerFacade.testServerConnection(server.getUrl(), server.getUsername(),
                    server.getPassword());
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
                projects = Collections.emptyList();
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
                for (JIRAConstant status : statuses) {
                    CachedIconLoader.getIcon(status.getIconUrl());
                }
            } catch (JIRAException e) {
                PluginUtil.getLogger().error(e.getMessage());
                statuses = Collections.emptyList();
            }
        }

        return statuses;
    }

    public List<JIRAConstant> getIssueTypes(JIRAProject project) {
		List<JIRAConstant> issueTypes = project == null ? globalIssueTypes : issueTypesCache.get(project.getKey());

		if (issueTypes == null) {
			List<JIRAConstant> retrieved;
			try {
                if (project == null) {
                    retrieved = jiraServerFacade.getIssueTypes(server);
                } else {
                    retrieved = jiraServerFacade.getIssueTypesForProject(server, Long.toString(project.getId()));
                }
                issueTypes = new ArrayList<JIRAConstant>(retrieved.size() + 1);
                issueTypes.add(new JIRAIssueTypeBean(ANY_ID, "Any", null));
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
					issueTypes = Collections.emptyList();
				}
            }
        }
        return issueTypes;
    }

    public List<JIRAQueryFragment> getSavedFilters() {
        if (savedFilters == null) {
            try {
                savedFilters = jiraServerFacade.getSavedFilters(server);
            } catch (JIRAException e) {
                PluginUtil.getLogger().error(e.getMessage());
                savedFilters = Collections.emptyList();
            }
        }
        return savedFilters;
    }

    public List<JIRAConstant> getPriorities() {
        if (priorities == null) {
            try {
                List<JIRAConstant> retrieved = jiraServerFacade.getPriorities(server);
                priorities = new ArrayList<JIRAConstant>(retrieved.size() + 1);
                priorities.add(new JIRAPriorityBean(ANY_ID, "Any", null));
                priorities.addAll(retrieved);
                for (JIRAConstant priority : priorities) {
                    CachedIconLoader.getIcon(priority.getIconUrl());
                }
            } catch (JIRAException e) {
                PluginUtil.getLogger().error(e.getMessage());
                priorities = Collections.emptyList();
            }
        }
        return priorities;
    }

    public List<JIRAResolutionBean> getResolutions() {
        if (resolutions == null) {
            try {
                List<JIRAResolutionBean> retrieved = jiraServerFacade.getResolutions(server);
                resolutions = new ArrayList<JIRAResolutionBean>(retrieved.size() + 1);
                resolutions.add(new JIRAResolutionBean(ANY_ID, "Any"));
                resolutions.add(new JIRAResolutionBean(UNRESOLVED_ID, "Unresolved"));
                resolutions.addAll(retrieved);
            } catch (JIRAException e) {
                PluginUtil.getLogger().error(e.getMessage());
                resolutions = Collections.emptyList();
            }
        }
        return resolutions;
    }

    private List<JIRAVersionBean> getAllVersions(JIRAProject project) {
		List<JIRAVersionBean> versions = null;
		if (project != null) {
			versions = serverVersionsCache.get(project.getKey());
		}
		if (versions == null) {
			try {
				if (project != null) {
					versions = jiraServerFacade.getVersions(server, project.getKey());
					serverVersionsCache.put(project.getKey(), versions);
				} else {
					versions = Collections.emptyList();
				}

			} catch (JIRAException e) {
				PluginUtil.getLogger().error(e.getMessage());
				versions = Collections.emptyList();
			}
		}
        return versions;
    }

    public List<JIRAVersionBean> getVersions(JIRAProject project) {
		List<JIRAVersionBean> versions;
		if (project != null) {
			List<JIRAVersionBean> retrieved = getAllVersions(project);
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
				versions = Collections.emptyList();
			}
		} else {
			versions = Collections.emptyList();
		}

        return versions;
    }

    public List<JIRAFixForVersionBean> getFixForVersions(JIRAProject project) {
		List<JIRAFixForVersionBean> fixForVersions;
		if (project != null) {
			List<JIRAVersionBean> retrieved = getAllVersions(project);
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
				fixForVersions = Collections.emptyList();
			}
		} else {
			fixForVersions = Collections.emptyList();
		}
        return fixForVersions;
    }

    public List<JIRAComponentBean> getComponents(JIRAProject project) {
		List<JIRAComponentBean> components = null;
		if (project != null) {
			components = componentsCache.get(project.getKey());
		}
		if (components == null) {
            try {
                if (project != null) {
                    List<JIRAComponentBean> retrieved = jiraServerFacade.getComponents(server, project.getKey());

                    components = new ArrayList<JIRAComponentBean>(retrieved.size() + 1);
                    components.add(new JIRAComponentBean(ANY_ID, "Any"));
                    components.add(new JIRAComponentBean(NO_COMPONENT_ID, "No component"));
                    components.addAll(retrieved);

                    componentsCache.put(project.getKey(), components);
                } else {
                    components = Collections.emptyList();
                }
            } catch (JIRAException e) {
                PluginUtil.getLogger().error(e.getMessage());
                components = Collections.emptyList();
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
