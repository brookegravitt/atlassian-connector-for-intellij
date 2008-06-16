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

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.*;

public class JIRAServer {
    private static final int VERSION_SPECIAL_VALUES_COUNT = 4;
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
    private List<JIRAConstant> statuses;

    private List<JIRAQueryFragment> savedFilters;
    private List<JIRAVersionBean> versions;
    private List<JIRAFixForVersionBean> fixForVersions;
    private List<JIRAConstant> priorieties;
    private List<JIRAResolutionBean> resolutions;

    private List<JIRAConstant> issueTypes;
    private List<JIRAVersionBean> serverVersions;
    private List<JIRAComponentBean> components;

    private Map<String, List<JIRAConstant>> issueTypesCache;
    private Map<String, List<JIRAVersionBean>> serverVersionsCache;
    private Map<String, List<JIRAComponentBean>> componentsCache;

    private JIRAProject currentProject = null;

    private final JIRAServerFacade jiraServerFacade;

    public JIRAServer(JIRAServerFacade jiraServerFacade) {
        this.jiraServerFacade = jiraServerFacade;
        this.issueTypesCache = new HashMap<String, List<JIRAConstant>>();
        this.serverVersionsCache = new HashMap<String, List<JIRAVersionBean>>();
        this.componentsCache = new HashMap<String, List<JIRAComponentBean>>();
    }

    public JIRAServer(Server server, JIRAServerFacade jiraServerFacade) {
        this(jiraServerFacade);
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public boolean checkServer() {
        try {
            jiraServerFacade.testServerConnection(server.getUrlString(), server.getUserName(),
                    server.transientGetPasswordString());
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
                for (JIRAConstant status : statuses) {
                    CachedIconLoader.getIcon(status.getIconUrl());
                }
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

                for (JIRAConstant issueType : issueTypes) {
                    CachedIconLoader.getIcon(issueType.getIconUrl());
                }

                if (currentProject != null) {
                    issueTypesCache.put(currentProject.getKey(), issueTypes);
                } else {
                    issueTypesCache.put("", issueTypes);
                }
            } catch (JIRAException e) {
                PluginUtil.getLogger().error(e.getMessage());
                if (issueTypesCache.containsKey("")) {
                    issueTypes = issueTypesCache.get("");
                    if (currentProject != null) {
                        issueTypesCache.put(currentProject.getKey(), issueTypes);
                    }
                } else {
                    issueTypes = Collections.EMPTY_LIST;
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
                savedFilters = Collections.EMPTY_LIST;
            }
        }
        return savedFilters;
    }

    public List<JIRAConstant> getPriorieties() {
        if (priorieties == null) {
            try {
                List<JIRAConstant> retrieved = jiraServerFacade.getPriorities(server);
                priorieties = new ArrayList(retrieved.size() + 1);
                priorieties.add(new JIRAPriorityBean(ANY_ID, "Any", null));
                priorieties.addAll(retrieved);
                for (JIRAConstant priority : priorieties) {
                    CachedIconLoader.getIcon(priority.getIconUrl());
                }
            } catch (JIRAException e) {
                PluginUtil.getLogger().error(e.getMessage());
                priorieties = Collections.EMPTY_LIST;
            }
        }
        return priorieties;
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
                    serverVersionsCache.put(currentProject.getKey(), serverVersions);
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
            } else {
                fixForVersions = Collections.EMPTY_LIST;
            }
        }
        return fixForVersions;
    }

    public List<JIRAComponentBean> getComponents() {
        if (components == null) {
            try {
                if (currentProject != null) {
                    List<JIRAComponentBean> retrieved = jiraServerFacade.getComponents(server, currentProject.getKey());

                    components = new ArrayList<JIRAComponentBean>(retrieved.size() + 1);
                    components.add(new JIRAComponentBean(ANY_ID, "Any"));
                    components.add(new JIRAComponentBean(NO_COMPONENT_ID, "No component"));
                    components.addAll(retrieved);

                    componentsCache.put(currentProject.getKey(), components);
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
        if (currentProject != null) {
            if (serverVersionsCache.containsKey(currentProject.getKey())) {
                serverVersions = serverVersionsCache.get(currentProject.getKey());
            } else {
                serverVersions = null;
            }
            versions = null;
            fixForVersions = null;
            if (componentsCache.containsKey(currentProject.getKey())) {
                components = componentsCache.get(currentProject.getKey());
            } else {
                components = null;
            }
            if (issueTypesCache.containsKey(currentProject.getKey())) {
                issueTypes = issueTypesCache.get(currentProject.getKey());
            } else {
                issueTypes = null;
            }
        } else {
            serverVersions = null;
            components = null;
            issueTypes = null;
        }
    }
}
