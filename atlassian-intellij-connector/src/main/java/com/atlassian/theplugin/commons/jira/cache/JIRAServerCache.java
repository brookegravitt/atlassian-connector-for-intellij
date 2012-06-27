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

package com.atlassian.theplugin.commons.jira.cache;

import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAFixForVersionBean;
import com.atlassian.connector.commons.jira.beans.JIRAIssueTypeBean;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAProjectBean;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRASecurityLevelBean;
import com.atlassian.connector.commons.jira.beans.JIRAStatusBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.cache.CacheConstants;
import com.atlassian.connector.commons.jira.cache.CachedIconLoader;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.intellij.openapi.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JIRAServerCache {
    private static final int VERSION_SPECIAL_VALUES_COUNT = 4;
    private final JiraServerData jiraServerData;
    private boolean validServer;
    private String errorMessage;

    private List<JIRAProject> projects;
    private List<JIRAConstant> statuses;

    private List<JIRAQueryFragment> savedFilters;
    private List<JIRAPriorityBean> priorities;
    private List<JIRAResolutionBean> resolutions;
    private List<JIRAConstant> globalIssueTypes;
    private List<JIRAConstant> globalSubtaskIssueTypes;

    private final Map<String, List<JIRAConstant>> issueTypesCache;
    private final Map<String, List<JIRAConstant>> subtaskIssueTypesCache;
    private final Map<String, List<JIRAVersionBean>> serverVersionsCache;
    private final Map<String, List<JIRAComponentBean>> componentsCache;
    private final HashMap<String, String> usersMap;
    private HashMap<String, List<JIRASecurityLevelBean>> securityLevels;

    private final JiraServerFacade jiraServerFacade;

    public JIRAServerCache(JiraServerData jiraServerData, JiraServerFacade jiraServerFacade) {
        this.jiraServerFacade = jiraServerFacade;
        this.issueTypesCache = new HashMap<String, List<JIRAConstant>>();
        this.subtaskIssueTypesCache = new HashMap<String, List<JIRAConstant>>();
        this.serverVersionsCache = new HashMap<String, List<JIRAVersionBean>>();
        this.componentsCache = new HashMap<String, List<JIRAComponentBean>>();
        this.jiraServerData = jiraServerData;
        this.usersMap = new HashMap<String, String>();
        this.securityLevels = new HashMap<String, List<JIRASecurityLevelBean>>();
    }

    public JiraServerData getJiraServerData() {
        return jiraServerData;
    }

    public boolean checkServer() throws RemoteApiException {
        try {
            jiraServerFacade.testServerConnection(jiraServerData);
            validServer = true;

        } catch (RemoteApiLoginException e) {
            throw e;

        } catch (RemoteApiException e) {
            errorMessage = e.getMessage();
            throw e;
        }
        return validServer;
    }

    public List<Pair<String, String>> getUsers() {
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        for (String userId : usersMap.keySet()) {
            list.add(new Pair(userId, usersMap.get(userId)));
        }

        return list;
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(final String projectKey) throws RemoteApiException,
            JIRAException {

        if (!securityLevels.containsKey(projectKey)) {
            securityLevels.put(projectKey, jiraServerFacade.getSecurityLevels(jiraServerData, projectKey));
        }

        return securityLevels.get(projectKey);
    }


    public List<JIRAProject> getProjects(boolean fromCacheOnly) throws JIRAException {
        if (projects == null && !fromCacheOnly) {
            try {
                List<JIRAProject> retrieved = jiraServerFacade.getProjects(jiraServerData);
                projects = new ArrayList<JIRAProject>();
                projects.add(new JIRAProjectBean(CacheConstants.ANY_ID, "Any"));
                projects.addAll(retrieved);
            } catch (JIRAException e) {
                errorMessage = e.getMessage();
                throw e;
            }
        }
        return projects;
    }

    public List<JIRAConstant> getStatuses() throws JIRAException {
        if (statuses == null) {
            try {
                List<JIRAConstant> retrieved = jiraServerFacade.getStatuses(jiraServerData);
                statuses = new ArrayList<JIRAConstant>(retrieved.size() + 1);
                statuses.add(new JIRAStatusBean(CacheConstants.ANY_ID, "Any", null));
                statuses.addAll(retrieved);
                for (JIRAConstant status : statuses) {
                    CachedIconLoader.getIcon(status.getIconUrl());
                }
            } catch (JIRAException e) {
                errorMessage = e.getMessage();
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
     */
    public List<JIRAConstant> getIssueTypes(JIRAProject project, boolean includeAny) throws JIRAException {
        List<JIRAConstant> issueTypes = project == null ? globalIssueTypes : issueTypesCache.get(project.getKey());

        if (issueTypes == null) {
            List<JIRAConstant> retrieved;
            try {
                if (project == null || project.getKey() == null) {
                    retrieved = jiraServerFacade.getIssueTypes(jiraServerData);
                } else {
                    retrieved = jiraServerFacade.getIssueTypesForProject(jiraServerData, Long.toString(project.getId()));
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
                if (globalIssueTypes != null) {
                    issueTypes = globalIssueTypes;
                } else {
                    throw e;
                }
            }
        }

        List<JIRAConstant> result = new ArrayList<JIRAConstant>();
        if (includeAny) {
            result.add(new JIRAIssueTypeBean(CacheConstants.ANY_ID, "Any", null));
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
                    retrieved = jiraServerFacade.getSubtaskIssueTypes(jiraServerData);
                } else {
                    retrieved = jiraServerFacade.getSubtaskIssueTypesForProject(jiraServerData, Long.toString(project.getId()));
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
                if (globalSubtaskIssueTypes != null) {
                    subtaskTypes = globalSubtaskIssueTypes;
                } else {
                    errorMessage = e.getMessage();
                    throw e;
                }
            }
        }

        return subtaskTypes;
    }

    public List<JIRAQueryFragment> getSavedFilters() throws JIRAException {
        if (savedFilters == null) {
            try {
                savedFilters = jiraServerFacade.getSavedFilters(jiraServerData);
            } catch (JIRAException e) {
                errorMessage = e.getMessage();
                throw e;
            }
        }
        return savedFilters;
    }

    public List<JIRAPriorityBean> getPriorities(boolean includeAny) throws JIRAException {
        if (priorities == null) {
            try {
                List<JIRAPriorityBean> retrieved = jiraServerFacade.getPriorities(jiraServerData);
                priorities = new ArrayList<JIRAPriorityBean>(retrieved.size() + 1);
                priorities.addAll(retrieved);
                for (JIRAConstant priority : priorities) {
                    CachedIconLoader.getIcon(priority.getIconUrl());
                }
            } catch (JIRAException e) {
                errorMessage = e.getMessage();
                throw e;
            }
        }

        List<JIRAPriorityBean> result = new ArrayList<JIRAPriorityBean>();
        if (includeAny) {
            result.add(new JIRAPriorityBean(CacheConstants.ANY_ID, -1, "Any", null));
        }
        result.addAll(priorities);
        return result;
    }

    public List<JIRAResolutionBean> getResolutions(boolean includeAnyAndUnknown) throws JIRAException {
        if (resolutions == null) {
            try {
                List<JIRAResolutionBean> retrieved = jiraServerFacade.getResolutions(jiraServerData);
                resolutions = new ArrayList<JIRAResolutionBean>(retrieved.size());
                resolutions.addAll(retrieved);
            } catch (JIRAException e) {
                errorMessage = e.getMessage();
                throw e;
            }
        }
        List<JIRAResolutionBean> result;
        result = new ArrayList<JIRAResolutionBean>();
        if (includeAnyAndUnknown) {
            result.add(new JIRAResolutionBean(CacheConstants.ANY_ID, "Any"));
            result.add(new JIRAResolutionBean(CacheConstants.UNRESOLVED_ID, "Unresolved"));
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
                    versions = jiraServerFacade.getVersions(jiraServerData, project.getKey());
                    Collections.reverse(versions);
                    serverVersionsCache.put(project.getKey(), versions);
                } else {
                    versions = Collections.emptyList();
                }

            } catch (JIRAException e) {
                errorMessage = e.getMessage();
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
                    versions.add(new JIRAVersionBean(CacheConstants.ANY_ID, "Any", false));
                    versions.add(new JIRAVersionBean(CacheConstants.NO_VERSION_ID, "No version", false));
                    versions.add(new JIRAVersionBean(CacheConstants.UNRELEASED_VERSION_ID, "Unreleased versions", false));
                }

                for (JIRAQueryFragment jiraQueryFragment : retrieved) {
                    if (!((JIRAVersionBean) jiraQueryFragment).isReleased()) {
                        versions.add((JIRAVersionBean) jiraQueryFragment);
                    }
                }
                if (includeSpecialValues) {
                    versions.add(new JIRAVersionBean(CacheConstants.RELEASED_VERSION_ID, "Released versions", true));
                }
                for (JIRAQueryFragment jiraQueryFragment : retrieved) {
                    if (((JIRAVersionBean) jiraQueryFragment).isReleased()) {
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
                    fixForVersions.add(new JIRAFixForVersionBean(CacheConstants.ANY_ID, "Any", false));
                    fixForVersions.add(new JIRAFixForVersionBean(CacheConstants.NO_VERSION_ID, "No version", false));
                    fixForVersions.add(new JIRAFixForVersionBean(CacheConstants.UNRELEASED_VERSION_ID,
                            "Unreleased versions", true));
                }

                for (JIRAVersionBean jiraQueryFragment : retrieved) {
                    if (!jiraQueryFragment.isReleased()) {
                        fixForVersions.add(new JIRAFixForVersionBean(jiraQueryFragment));
                    }
                }

                if (includeSpecialValues) {
                    fixForVersions.add(new JIRAFixForVersionBean(CacheConstants.RELEASED_VERSION_ID,
                            "Released versions", false));
                }

                for (JIRAVersionBean jiraQueryFragment : retrieved) {
                    if (jiraQueryFragment.isReleased()) {
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
                    List<JIRAComponentBean> retrieved = jiraServerFacade.getComponents(jiraServerData, project.getKey());

                    components = new ArrayList<JIRAComponentBean>(retrieved.size() + 1);
                    if (includeSpecialValues) {
                        components.add(new JIRAComponentBean(CacheConstants.ANY_ID, "Any"));
                        components.add(new JIRAComponentBean(CacheConstants.UNKNOWN_COMPONENT_ID, "Unknown"));
                    }
                    components.addAll(retrieved);

                    componentsCache.put(project.getKey(), components);
                } else {
                    components = Collections.emptyList();
                }
            } catch (JIRAException e) {
                errorMessage = e.getMessage();
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

    public void addUser(String userId, String userName) {
        usersMap.put(userId, userName);
    }
  
}

