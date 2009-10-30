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

package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.api.commons.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.jira.DeactivateIssueResultHandler;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.cache.RecentlyOpenIssuesCache;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 * Date: Oct 22, 2009
 */
public final class PluginTaskManager {
    private static final String TASK_MANAGER_IMPL_CLASS = "com.intellij.tasks.impl.TaskManagerImpl";
    private static final String JIRA_REPOSITORY_CLASS = "com.intellij.tasks.jira.JiraRepository";
    private static final String LOCAL_TASK_IMPL_CLASS = "com.intellij.tasks.impl.LocalTaskImpl";
    private static final String TASK_REPOSITORY_TYPE_CLASS = "com.intellij.tasks.TaskRepositoryType";
    private static final String TASK_REPOSITORY_CLASS = "com.intellij.tasks.TaskRepository";
    private static final String TASK_CLASS = "com.intellij.tasks.Task";
    private static final String LOCAL_TASK_CLASS = "com.intellij.tasks.LocalTask";
    private static final String CHANGE_LIST_INFO = "com.intellij.tasks.ChangeListInfo";
    private static final String LIST_CLASS = "java.util.ArrayList";

    private static Map<Project, PluginTaskManager> managers = new HashMap<Project, PluginTaskManager>();
    private final Project project;
    private ChangeListListener myListener;
    private ClassLoader classLoader;
    private Class taskManagerClass;
    private Object taskManagerObj;


    private PluginTaskManager(final Project project) {
        this.project = project;

        if (isValidIdeaVersion()) {
            classLoader = getTaskManagerDescriptor().getPluginClassLoader();
            try {
                taskManagerClass = classLoader.loadClass(TASK_MANAGER_IMPL_CLASS);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot get task class loader", e);
            }

            taskManagerObj = getTaskManager();
            myListener = new LocalChangeListAdapter(project);
        }

    }


    public static PluginTaskManager getInstance(final Project project) {
        if (!managers.containsKey(project)) {
            managers.put(project, new PluginTaskManager(project));
        }

        return managers.get(project);
    }

    public synchronized void addChangeListListener() {
        if (!isValidIdeaVersion()) {
            return;
        }

        ChangeListManager.getInstance(project).addChangeListListener(myListener);
    }

    public synchronized void removeChangeListListener() {
        if (!isValidIdeaVersion()) {
            return;
        }
        ChangeListManager.getInstance(project).removeChangeListListener(myListener);
    }

    //todo fix this to shitch to task that is linked to Default changeset
    public void deactivateToDefaultTask() {
        if (!isValidIdeaVersion()) {
            return;
        }

        Object defaultTaskObj = getDefaultTask();
        if (defaultTaskObj != null) {
            activateTask(defaultTaskObj, false, false);
        }
    }

    public void activateLocalTask(ActiveJiraIssue issue) {
        if (!isValidIdeaVersion()) {
            return;
        }
        Object foundTask = null;

        PluginTaskManager taskManager = PluginTaskManager.getInstance(project);
        foundTask = taskManager.findLocalTaskById(issue.getIssueKey());

        if (foundTask != null) {
            if (!taskManager.getActiveTaskId().equals(issue.getIssueKey())) {
                PluginTaskManager.getInstance(project).activateTask(foundTask, false, false);
            }
        } else {

            ServerData server = IdeaHelper.getProjectCfgManager(project).getServerr(issue.getServerId());
            foundTask = PluginTaskManager.getInstance(project).createLocalTask(issue.getIssueKey(), server);
            PluginTaskManager.getInstance(project).activateTask(foundTask, true, true);
        }
    }

    private Object getChangeListTask(ChangeList changeList) {
        Object[] localTasks = getLocalTasks();
        if (localTasks != null) {
            for (Object t : localTasks) {
                String changelistId = getChangeListId(t);
                if (changelistId != null && changelistId.equals(getLocalChangeListId(changeList))) {
                    return t;
                }
            }
        }
        return null;
    }

    @Nullable
    private String getLocalChangeListId(ChangeList localList) {
        try {
            Class localChangeListClass = project.getClass().getClassLoader().loadClass("com.intellij.openapi.vcs.changes.LocalChangeList");
            Method getIdMethod = localChangeListClass.getMethod("getId");
            Object idObj = getIdMethod.invoke(localList);
            if (idObj != null) {
                return idObj.toString();
            }
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot get local tasks ", e);
        } catch (NoSuchMethodException e) {
            PluginUtil.getLogger().error("Cannot get local tasks ", e);
        } catch (InvocationTargetException e) {
            PluginUtil.getLogger().error("Cannot get local tasks ", e);
        } catch (IllegalAccessException e) {
            PluginUtil.getLogger().error("Cannot get local tasks ", e);
        }

        return null;
    }

    @Nullable
    private Object getDefaultTask() {
        ChangeListManager manager = ChangeListManager.getInstance(project);
        if (manager != null) {
            ChangeList defaultChangeList = getDefaultChangeList(project);
            return getChangeListTask(defaultChangeList);
        }

        return null;
    }


    //assume that RO change list is default
    @Nullable
    private LocalChangeList getDefaultChangeList(Project project) {
        ChangeListManager manager = ChangeListManager.getInstance(project);
        for (LocalChangeList l : manager.getChangeLists()) {
            if (l.isReadOnly()) {
                return l;
            }
        }

        return null;
    }

    private Object[] getLocalTasks() {
        for (IdeaPluginDescriptor descriptor : ApplicationManager.getApplication().getPlugins()) {
            if (descriptor.getPluginId().getIdString().equals("com.intellij.tasks")) {
                try {

                    Method getManager = taskManagerClass.getMethod("getManager", Project.class);

                    Object taskManager = getManager.invoke(null, project);
                    Method getLocalTasks = taskManagerClass.getMethod("getLocalTasks");
                    Object[] localTasksObj = (Object[]) getLocalTasks.invoke(taskManager);

                    return localTasksObj;
                } catch (NoSuchMethodException e) {
                    PluginUtil.getLogger().error("Cannot get local tasks ", e);
                } catch (InvocationTargetException e) {
                    PluginUtil.getLogger().error("Cannot get local tasks ", e);
                } catch (IllegalAccessException e) {
                    PluginUtil.getLogger().error("Cannot get local tasks ", e);
                }
            }
        }

        return null;

    }

    @Nullable
    private Object getTaskManager() {

        IdeaPluginDescriptor descriptor = getTaskManagerDescriptor();
        if (descriptor != null) {
            try {
                Method getManager = taskManagerClass.getMethod("getManager", Project.class);
                Object taskManager = getManager.invoke(null, project);
                return taskManager;
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot load class TaskManager", e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot load class TaskManager", e);
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot load class TaskManager", e);
            }
        }

        return null;
    }

    private IdeaPluginDescriptor getTaskManagerDescriptor() {
        for (IdeaPluginDescriptor descriptor : ApplicationManager.getApplication().getPlugins()) {
            if (descriptor.getPluginId().getIdString().equals("com.intellij.tasks")) {
                return descriptor;
            }
        }
        return null;
    }


    @Nullable
    private Object findLocalTaskById(String taskId) {
        Object[] localTasks = getLocalTasks();
        if (localTasks != null) {
            for (Object t : localTasks) {
                String localTaskId = getTaskId(t);
                if (localTaskId != null && localTaskId.equals(taskId)) {
                    return t;
                }
            }
        }
        return null;
    }

    @Nullable
    private Object findLocalTaskBySummary(String summary) {
        Object[] localTasks = getLocalTasks();
        if (localTasks != null) {
            for (Object t : localTasks) {
                String localTaskId = getTaskSummary(t);
                if (localTaskId != null && localTaskId.equals(summary)) {
                    return t;
                }
            }
        }
        return null;
    }

    @Nullable
    private Object createLocalTask(final String taskId, ServerData server) {

        if (classLoader != null) {
            try {
                Object jiraRepository = getJiraRepository(server);

                if (jiraRepository != null) {
                    Class jiraRepositoryClass = classLoader.loadClass(JIRA_REPOSITORY_CLASS);
                    Method findTask = jiraRepositoryClass.getMethod("findTask", String.class);
                    Object task = findTask.invoke(jiraRepository, taskId);
                    return task;
                }
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot create local task", e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot create local task", e);
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot create local task", e);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot create local task", e);
            }

        }
        return null;
    }

    private String getTaskId(final Object task) {
        if (classLoader != null) {
            try {

                Class localTaskClass = classLoader.loadClass(LOCAL_TASK_IMPL_CLASS);
                Method getTaskId = localTaskClass.getMethod("getId");

                Object localTaskObj = getTaskId.invoke(task);
                return localTaskObj.toString();
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot get local task id", e);
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot get local task id", e);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot get local task id", e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot get local task id", e);
            }
        }

        return null;
    }

    private String getTaskSummary(final Object task) {
        if (classLoader != null) {
            try {

                Class localTaskClass = classLoader.loadClass(LOCAL_TASK_IMPL_CLASS);
                Method getSummaryMethod = localTaskClass.getMethod("getSummary");

                Object localTaskObj = getSummaryMethod.invoke(task);
                return localTaskObj.toString();
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot get local task summary", e);
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot get local task summary", e);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot get local task summary", e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot get local task summary", e);
            }
        }

        return null;
    }

    @Nullable
    private String getChangeListId(final Object task) {
        if (classLoader != null) {
            try {

                Class localTaskClass = classLoader.loadClass(LOCAL_TASK_IMPL_CLASS);
                Class changeListInfoClass = classLoader.loadClass(CHANGE_LIST_INFO);
                Method getChangeListsMethod = localTaskClass.getMethod("getChangeLists");
                Field id = changeListInfoClass.getField("id");

                List<Object> changeSetList = (List<Object>) getChangeListsMethod.invoke(task);
                if (changeSetList != null && changeSetList.size() > 0) {
                    return id.get(changeSetList.get(0)).toString();
                }
                return null;
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot get local task associated change list", e);
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot get local task associated change list", e);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot get local task associated change list", e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot get local task associated change list", e);
            } catch (NoSuchFieldException e) {
                PluginUtil.getLogger().error("Cannot get local task associated change list", e);
            }
        }

        return null;
    }

    private void activateTask(final Object task, boolean clearContext, boolean createChangeset) {
        if (classLoader != null && task != null) {
            try {

                Class taskClass = classLoader.loadClass(TASK_CLASS);

                if (taskManagerObj != null) {
                    Method activateLocalTask = taskManagerClass.getMethod("activateTask", taskClass,
                            Boolean.TYPE, Boolean.TYPE);
                    activateLocalTask.invoke(taskManagerObj, task, clearContext, createChangeset);
                }
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot activate local task", e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot activate local task", e);
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot activate local task", e);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot activate local task", e);
            }
        }
    }

    //get or creates
    private Object getJiraRepository(final ServerData jiraServer) {

        if (classLoader != null) {
            try {
                taskManagerClass = classLoader.loadClass(TASK_MANAGER_IMPL_CLASS);

                if (taskManagerObj != null) {
                    //@todo we should check repository type and username password not to duplicate
                    Method getAllRepositoryTypes = taskManagerClass.getMethod("getAllRepositoryTypes");
                    Class taskRepositoryTypeClass = classLoader.loadClass(TASK_REPOSITORY_TYPE_CLASS);
                    Class taskRepositoryClass = classLoader.loadClass(TASK_REPOSITORY_CLASS);
                    Class listClass = classLoader.loadClass(LIST_CLASS);

                    Object[] repoTypes = (Object[]) getAllRepositoryTypes.invoke(taskManagerObj);


                    for (Object repoType : repoTypes) {
                        Method getName = taskRepositoryTypeClass.getMethod("getName");
                        Object name = getName.invoke(repoType);
                        if (name != null && name.toString().equals("JIRA")) {
                            Method getRepositories = taskRepositoryTypeClass.getMethod("getRepositories");
                            List jiraRepos = (List) getRepositories.invoke(repoType);
                            for (Object jiraRepo : jiraRepos) {
                                Method getUrl = taskRepositoryClass.getMethod("getUrl");
                                Object url = getUrl.invoke(jiraRepo);
                                if (url != null && url.toString().equals(jiraServer.getUrl())) {
                                    return jiraRepo;
                                }
                            }

                            //create jira repo
                            Method createReposirtory = taskRepositoryTypeClass.getMethod("createRepository");
                            Object newRepository = createReposirtory.invoke(repoType);
                            Class jiraRepositoryClass = classLoader.loadClass(JIRA_REPOSITORY_CLASS);

                            Method setUrl = jiraRepositoryClass.getMethod("setUrl", String.class);
                            setUrl.invoke(newRepository, jiraServer.getUrl());

                            Method setUsername = jiraRepositoryClass.getMethod("setUsername", String.class);
                            setUsername.invoke(newRepository, jiraServer.getUsername());

                            Method setPassword = jiraRepositoryClass.getMethod("setPassword", String.class);
                            setPassword.invoke(newRepository, jiraServer.getPassword());

                            Method setShared = jiraRepositoryClass.getMethod("setShared", Boolean.TYPE);
                            setShared.invoke(newRepository, true);
                            List<Object> newJiraReposList = (List<Object>) listClass.newInstance();
                            newJiraReposList.addAll(jiraRepos);
                            newJiraReposList.add(newRepository);

                            Method setRepositories = taskManagerClass.getMethod("setRepositories", List.class,
                                    taskRepositoryTypeClass);
                            setRepositories.invoke(taskManagerObj, newJiraReposList, repoType);
                            return newRepository;
                        }
                    }

                }

            } catch (ClassNotFoundException
                    e) {
                PluginUtil.getLogger().error("Cannot get JIRA repository", e);
            } catch (NoSuchMethodException
                    e) {
                PluginUtil.getLogger().error("Cannot get JIRA repository", e);
            } catch (InvocationTargetException
                    e) {
                PluginUtil.getLogger().error("Cannot get JIRA repository", e);
            } catch (IllegalAccessException
                    e) {
                PluginUtil.getLogger().error("Cannot get JIRA repository", e);
            } catch (InstantiationException e) {
                PluginUtil.getLogger().error("Cannot get JIRA repository", e);
            }
        }
        return null;
    }


    private String getActiveTaskUrl() {
        try {
            if (classLoader != null) {
                Class localTaskClass = classLoader.loadClass(LOCAL_TASK_CLASS);
                if (taskManagerObj != null) {
                    Method getActiveTaskMethod = taskManagerClass.getMethod("getActiveTask");
                    Object localTaskObj = getActiveTaskMethod.invoke(taskManagerObj);
                    if (localTaskObj != null) {
                        Method getIssueUrl = localTaskClass.getMethod("getIssueUrl");
                        Object activeTaskUrl = getIssueUrl.invoke(localTaskObj);
                        if (activeTaskUrl != null) {
                            return activeTaskUrl.toString();
                        }
                    }
                }
            }

        } catch (InvocationTargetException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url", e);
        } catch (NoSuchMethodException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url", e);
        } catch (IllegalAccessException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url", e);
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url", e);
        }

        return null;
    }

    private final class LocalChangeListAdapter extends ChangeListAdapter {
        private final Project project;

        private LocalChangeListAdapter(final Project project) {
            this.project = project;
        }


        public void defaultListChanged(final ChangeList oldDefaultList, final ChangeList newDefaultList) {
            final String activeTaskUrl = getActiveTaskUrl();
            final JiraWorkspaceConfiguration conf =
                    IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);

            //removeChangeListListener();
            
            //switched to default task so deactivate issue
            if (((LocalChangeList) newDefaultList).getId().equals(getDefaultChangeList(project).getId())) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {

                        ActiveIssueUtils.deactivate(project, new DeactivateIssueResultHandler() {
                            public void success() {
                                if (conf != null) {
                                    conf.setActiveJiraIssuee(null);
                                }
                            }

                            public void failure(Throwable problem) {
                                if (conf != null) {
                                    conf.setActiveJiraIssuee(null);
                                }
                            }
                        });
                    }
                },  ModalityState.defaultModalityState());

                return;

            }
            if (activeTaskUrl != null) {
                final JiraServerData server = findJiraPluginJiraServer(activeTaskUrl);
                final String issueId = getActiveTaskId();
                final ActiveJiraIssueBean issue = new ActiveJiraIssueBean();
                issue.setIssueKey(issueId);
                issue.setServerId(server != null ? (ServerIdImpl) server.getServerId() : null);

                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {
                        JIRAIssueListModelBuilder builder = IdeaHelper.getJIRAIssueListModelBuilder(project);
                        if (server != null && builder != null) {
                            try {
                                if (builder.getModel().findIssue(issueId) == null) {
                                    JiraIssueAdapter issueAdapter = IntelliJJiraServerFacade.getInstance().getIssue(server,
                                            issueId);
                                    if (issueAdapter != null) {
                                        List list = new ArrayList();
                                        list.add(issueAdapter);
                                        builder.getModel().addIssues(list);
                                    }
                                }

                            } catch (JIRAException e) {
                                DialogWithDetails.showExceptionDialog(project,
                                        "Cannot fetch issue " + issueId + " from server " + server.getName(), e);
                            }


                            if ((ActiveIssueUtils.getActiveJiraIssue(project) == null && issueId != null)
                                    || (ActiveIssueUtils.getActiveJiraIssue(project) != null && issueId != null
                                    && !issueId.equals(ActiveIssueUtils.getActiveJiraIssue(project).getIssueKey()))) {
                                ActiveIssueUtils.activateIssue(project, null, issue, server, newDefaultList);
                            }
                        } else {
                            Messages.showInfoMessage("Cannot activate issue " + activeTaskUrl,
                                    PluginUtil.PRODUCT_NAME);
                        }


                    }
                }, ModalityState.defaultModalityState());
            }
        }
    }


    private JiraIssueAdapter findActiveJiraIssue(String issueUrl) {
        RecentlyOpenIssuesCache cache = IdeaHelper.getProjectComponent(project, RecentlyOpenIssuesCache.class);
        if (cache != null) {
            for (JiraIssueAdapter issue : cache.getLoadedRecenltyOpenIssues()) {
                if (issueUrl.equals(issue.getIssueUrl())) {
                    return issue;
                }
            }
        }
        return null;
    }

    private String getActiveTaskId() {
        try {
            if (classLoader != null) {
                Class localTaskClass = classLoader.loadClass(LOCAL_TASK_CLASS);
                if (taskManagerObj != null) {
                    Method getActiveTaskMethod = taskManagerClass.getMethod("getActiveTask");
                    Object localTaskObj = getActiveTaskMethod.invoke(taskManagerObj);
                    if (localTaskObj != null) {
                        Method getIdMethod = localTaskClass.getMethod("getId");
                        Object activeTaskUrl = getIdMethod.invoke(localTaskObj);
                        if (activeTaskUrl != null) {
                            return activeTaskUrl.toString();
                        }
                    }
                }
            }

        } catch (InvocationTargetException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url", e);
        } catch (NoSuchMethodException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url", e);
        } catch (IllegalAccessException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url", e);
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url", e);
        }

        return null;
    }

    @Nullable
    private JiraServerData findJiraPluginJiraServer(String issueUrl) {
        for (JiraServerData server : IdeaHelper.getProjectCfgManager(project).getAllEnabledJiraServerss()) {
            if (issueUrl.contains(server.getUrl())) {
                return server;
            }
        }

        return null;
    }

    private boolean isValidIdeaVersion() {
        return IdeaVersionFacade.getInstance().isIdea9();
    }

}