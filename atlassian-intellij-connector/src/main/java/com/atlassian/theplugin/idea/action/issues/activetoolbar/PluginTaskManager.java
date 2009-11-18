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

import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.jira.ActiveIssueResultHandler;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListAdapter;
import com.intellij.openapi.vcs.changes.ChangeListListener;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.Field;
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
    private static final String PLUGIN_ID_TASKS = "com.intellij.tasks";

    private static final String CANNOT_GET_ACTIVE_LOCAL_TASK_ISSUE_URL = "Cannot get active local task issue url";
    private static final String CANNOT_GET_JIRA_REPOSITORY = "Cannot get JIRA repository";
    private static final String CANNOT_ACTIVATE_LOCAL_TASK = "Cannot activate local task";
    private static final String CANNOT_GET_LOCAL_TASK_ASSOCIATED_CHANGE_LIST = "Cannot get local task associated change list";
    private static final String CANNOT_GET_LOCAL_TASK_SUMMARY = "Cannot get local task summary";
    private static final String CANNOT_GET_LOCAL_TASK_ID = "Cannot get local task id";
    private static final String CANNOT_CREATE_LOCAL_TASK = "Cannot create local task";
    private static final String CANNOT_LOAD_CLASS_TASK_MANAGER = "Cannot load class TaskManager";
    private static final String CANNOT_GET_LOCAL_TASKS = "Cannot get local tasks";

    private static Map<Project, PluginTaskManager> managers = new HashMap<Project, PluginTaskManager>();
    private final Project project;
    private ChangeListListener myListener;
    private ClassLoader classLoader;
    private Class taskManagerClass;
    private Object taskManagerObj;
    private boolean alreadyAdded = false;
    private static boolean actionsRegistered = false;


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

    /**
     * Because TasksToolbar DefaultActionGroup has no name. We search for specified class in DefaultActionGroup
     * from MainToolbar
     *
     * @return DefaultActionGroup
     */
    @Nullable
    private static DefaultActionGroup getTaskActionGroup() {
        DefaultActionGroup mainToolBar = (DefaultActionGroup) (ActionManagerImpl.getInstance().getAction("MainToolBar"));
        if (mainToolBar != null) {
            for (AnAction action : getGroupActionsOrStubs(mainToolBar)) {
                if (action instanceof DefaultActionGroup) {
                    DefaultActionGroup group = (DefaultActionGroup) action;
                    for (AnAction groupAction : getGroupActionsOrStubs(group)) {
                        if (groupAction.getClass().getName().equals("com.intellij.tasks.actions.SwitchTaskCombo")) {
                            return group;
                        }
                    }
                }
            }
        }

        return null;
    }

    private static AnAction[] getGroupActionsOrStubs(DefaultActionGroup group) {
        AnAction[] actions = new AnAction[0];
        ClassLoader classLoader = DefaultActionGroup.class.getClassLoader();

        if (classLoader != null) {
            actions = new AnAction[group.getChildrenCount()];
            try {
                Method getChildActionsOrStubsMethod = DefaultActionGroup.class.getMethod("getChildActionsOrStubs");
                actions = (AnAction[]) getChildActionsOrStubsMethod.invoke(group);
            } catch (Exception e) {
                PluginUtil.getLogger().error("Cannot get AnAction[] for group " + group.getTemplatePresentation().getText());
            }
        }

        return actions;
    }

    private static void removePluginTaskCombo() {
        DefaultActionGroup pluginTaskActions =
                (DefaultActionGroup) ActionManager.getInstance().getAction("ThePlugin.ActiveToolbar");
        DefaultActionGroup mainToolBar = (DefaultActionGroup) (ActionManagerImpl.getInstance().getAction("MainToolBar"));

        if (mainToolBar != null && pluginTaskActions != null) {
            mainToolBar.remove(pluginTaskActions);
        }
    }

    public static void organizeTaskActionsInToolbar() {
        if (!isValidIdeaVersion()) {
            return;
        }

        DefaultActionGroup tasksGroup = getTaskActionGroup();
        if (tasksGroup != null && !actionsRegistered) {
            DefaultActionGroup pluginTaskActions =
                    (DefaultActionGroup) ActionManager.getInstance().getAction("ThePlugin.TasksToolbar");
            if (pluginTaskActions != null) {
                tasksGroup.add(pluginTaskActions);
                actionsRegistered = true;
                removePluginTaskCombo();
            }
        }

    }

    public static PluginTaskManager getInstance(final Project project) {
        if (!managers.containsKey(project)) {
            managers.put(project, new PluginTaskManager(project));
        }

        return managers.get(project);
    }

    public synchronized void addChangeListListener() {
        if (!isValidIdeaVersion() && alreadyAdded) {
            return;
        }
        ChangeListManager.getInstance(project).addChangeListListener(myListener);
        alreadyAdded = true;
    }

    public synchronized void removeChangeListListener() {
        if (!isValidIdeaVersion()) {
            return;
        }
        ChangeListManager.getInstance(project).removeChangeListListener(myListener);
        alreadyAdded = false;
    }

    public void deactivateToDefaultTask() {
        if (!isValidIdeaVersion()) {
            return;
        }

        Object defaultTaskObj = getDefaultTask();
        if (defaultTaskObj != null) {
            activateTask(defaultTaskObj, false, false);
        }

        addChangeListListener();
    }


    @Nullable
    private String getActiveIssueUrl(String issueKey) {
        JIRAIssueListModelBuilder builder = IdeaHelper.getJIRAIssueListModelBuilder(project);
        if (builder != null && builder.getModel().findIssue(issueKey) != null) {
            JiraIssueAdapter issueAdapter = builder.getModel().findIssue(issueKey);
            return issueAdapter.getIssueUrl();
        }

        return null;
    }

    public void activateLocalTask(ActiveJiraIssue issue) {
        if (!isValidIdeaVersion()) {
            return;
        }
        Object foundTask;

        ServerData server = IdeaHelper.getProjectCfgManager(project).getServerr(issue.getServerId());
        foundTask = findLocalTaskByUrl(getActiveIssueUrl(issue.getIssueKey()));
        if (foundTask != null) {
            foundTask = findLocalTaskById(issue.getIssueKey());
        }

        //ADD or GET JiraRepository
        Object jiraRepository = getJiraRepository(server);
        if (foundTask != null) {
            if (!getActiveTaskId().equals(issue.getIssueKey())) {
                activateTask(foundTask, false, true);
            }
        } else {
            //todo search for issue ID and modify task insead of creating one
            foundTask = PluginTaskManager.getInstance(project).createLocalTask(issue.getIssueKey(), jiraRepository);
            activateTask(foundTask, true, true);
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
            Class localChangeListClass =
                    project.getClass().getClassLoader().loadClass("com.intellij.openapi.vcs.changes.LocalChangeList");
            Method getIdMethod = localChangeListClass.getMethod("getId");
            if (localList != null && getIdMethod != null) {
                Object idObj = getIdMethod.invoke(localList);
                if (idObj != null) {
                    return idObj.toString();
                }
            }
        } catch (Exception e) {
            PluginUtil.getLogger().error(PluginTaskManager.CANNOT_GET_LOCAL_TASKS, e);
        }

        return null;
    }

    @Nullable
    private Object getDefaultTask() {
        ChangeListManager manager = ChangeListManager.getInstance(project);
        if (manager != null) {
            ChangeList defaultChangeList = getDefaultChangeList();
            return getChangeListTask(defaultChangeList);
        }

        return null;
    }


    //assume that RO change list is default
    @Nullable
    private LocalChangeList getDefaultChangeList() {
        String defaultChangeListNameNoVcs = "Default task";
        String defaultChangeListNameVcs = "Default";
        LocalChangeList foundList = null;
        ChangeListManager manager = ChangeListManager.getInstance(project);
        for (LocalChangeList l : manager.getChangeLists()) {
            if (defaultChangeListNameNoVcs.equals(l.getName())) {
                foundList = l;
            }

            if (defaultChangeListNameVcs.equals(l.getName())) {
                foundList = l;
            }
        }

        return foundList;
    }

    private Object[] getLocalTasks() {
        for (IdeaPluginDescriptor descriptor : ApplicationManager.getApplication().getPlugins()) {
            if (descriptor.getPluginId().getIdString().equals(PLUGIN_ID_TASKS)) {
                try {

                    Method getManager = taskManagerClass.getMethod("getManager", Project.class);

                    Object taskManager = getManager.invoke(null, project);
                    Method getLocalTasks = taskManagerClass.getMethod("getLocalTasks");

                    return (Object[]) getLocalTasks.invoke(taskManager);
                } catch (Exception e) {
                    PluginUtil.getLogger().error(CANNOT_GET_LOCAL_TASKS, e);
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
                return getManager.invoke(null, project);
            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_LOAD_CLASS_TASK_MANAGER, e);
            }
        }

        return null;
    }

    private IdeaPluginDescriptor getTaskManagerDescriptor() {
        for (IdeaPluginDescriptor descriptor : ApplicationManager.getApplication().getPlugins()) {
            if (descriptor.getPluginId().getIdString().equals(PLUGIN_ID_TASKS)) {
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
    private Object findLocalTaskByUrl(String url) {
        Object[] localTasks = getLocalTasks();
        if (localTasks != null) {
            for (Object t : localTasks) {
                String localTaskId = getTaskUrl(t);
                if (localTaskId != null && localTaskId.equals(url)) {
                    return t;
                }
            }
        }
        return null;
    }


    @Nullable
    private Object createLocalTask(final String taskId, Object jiraRepository) {

        if (classLoader != null) {
            try {

                if (jiraRepository != null) {
                    Class jiraRepositoryClass = classLoader.loadClass(JIRA_REPOSITORY_CLASS);
                    Method findTask = jiraRepositoryClass.getMethod("findTask", String.class);
                    return findTask.invoke(jiraRepository, taskId);
                }
            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_CREATE_LOCAL_TASK, e);
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
            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_GET_LOCAL_TASK_ID, e);
            }
        }

        return null;
    }

    private String getTaskUrl(final Object task) {
        if (classLoader != null) {
            try {

                Class localTaskClass = classLoader.loadClass(LOCAL_TASK_IMPL_CLASS);
                Method getUrlMethod = localTaskClass.getMethod("getUrl");

                Object localTaskObj = getUrlMethod.invoke(task);
                return localTaskObj.toString();
            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_GET_LOCAL_TASK_SUMMARY, e);
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
            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_GET_LOCAL_TASK_ASSOCIATED_CHANGE_LIST, e);
            }
        }

        return null;
    }

    private void activateTask(final Object task, final boolean clearContext, final boolean createChangeset) {
        if (classLoader != null && task != null) {
            try {

                Class taskClass = classLoader.loadClass(TASK_CLASS);

                if (taskManagerObj != null) {
                    final Method activateLocalTask = taskManagerClass.getMethod("activateTask", taskClass,
                            Boolean.TYPE, Boolean.TYPE);
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            try {
                                activateLocalTask.invoke(taskManagerObj, task, clearContext, createChangeset);
                            } catch (Exception e) {
                                PluginUtil.getLogger().error(CANNOT_ACTIVATE_LOCAL_TASK, e);
                            }
                        }
                    });

                }
            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_ACTIVATE_LOCAL_TASK, e);
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

            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_GET_JIRA_REPOSITORY, e);
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

        } catch (Exception e) {
            PluginUtil.getLogger().error(CANNOT_GET_ACTIVE_LOCAL_TASK_ISSUE_URL, e);
        }

        return null;
    }

    private final class LocalChangeListAdapter extends ChangeListAdapter {
        private final Project project;

        private LocalChangeListAdapter(final Project project) {
            this.project = project;
        }


        public void defaultListChanged(final ChangeList oldDefaultList, final ChangeList newDefaultList) {
            String activeTaskUrl = getActiveTaskUrl();
            final String activeTaskId = getActiveTaskId();

            //removeChangeListListener();

            //switched to default task so silentDeactivate issue
            if (getLocalChangeListId(newDefaultList) != null && getLocalChangeListId(getDefaultChangeList()) != null
                    && getLocalChangeListId(newDefaultList).equals(getLocalChangeListId(getDefaultChangeList()))) {
                deactivateTask();

                return;

            }

            if (activeTaskUrl == null) {
                activeTaskUrl = getActiveIssueUrl(activeTaskId);
            }
            final String finalActiveTaskUrl = activeTaskUrl;


            if (activeTaskUrl != null) {
                final JiraServerData server = findJiraPluginJiraServer(activeTaskUrl);
                final String issueId = getActiveTaskId();
                final ActiveJiraIssueBean issue = new ActiveJiraIssueBean();
                issue.setIssueKey(issueId);
                issue.setServerId(server != null ? (ServerIdImpl) server.getServerId() : null);

                ApplicationManager.getApplication().invokeLater(
                        new LocalRunnable(issueId, server, issue, finalActiveTaskUrl, newDefaultList),
                        ModalityState.defaultModalityState());
            } else {

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Messages.showInfoMessage(project, "Cannot activate an issue " + getActiveTaskId() + "."
                                + "\nIssue without linked server.", PluginUtil.PRODUCT_NAME);
                    }
                });

                deactivateTask();
            }

        }

        private void deactivateTask() {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                private final JiraWorkspaceConfiguration conf =
                        IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);

                public void run() {

                    ActiveIssueUtils.deactivate(project, new ActiveIssueResultHandler() {
                        public void success() {
                            if (conf != null) {
                                conf.setActiveJiraIssuee(null);
                                addChangeListListener();
                            }
                        }

                        public void failure(Throwable problem) {

                            if (conf != null) {
                                PluginTaskManager.getInstance(project).activateLocalTask(conf.getActiveJiraIssuee());
                                addChangeListListener();
                            }
                        }

                        public void cancel(String problem) {
                            if (conf != null) {
                                PluginTaskManager.getInstance(project).activateLocalTask(conf.getActiveJiraIssuee());
                                addChangeListListener();
                            }
                        }
                    });
                }
            }, ModalityState.defaultModalityState());
        }


        final class LocalRunnable implements Runnable {
            private final String issueId;
            private final JiraServerData server;
            private final ActiveJiraIssueBean issue;
            private final String finalActiveTaskUrl;
            private final ChangeList newDefaultList;

            public LocalRunnable(String issueId, JiraServerData server, ActiveJiraIssueBean issue,
                                 String finalActiveTaskUrl, ChangeList newDefaultList) {
                this.issueId = issueId;
                this.server = server;
                this.issue = issue;
                this.finalActiveTaskUrl = finalActiveTaskUrl;
                this.newDefaultList = newDefaultList;
            }


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

                    } catch (final JIRAException e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                DialogWithDetails.showExceptionDialog(project,
                                        "Cannot fetch issue " + issueId + " from server " + server.getName(), e);
                            }
                        });

                    }

                    if ((ActiveIssueUtils.getActiveJiraIssue(project) == null && issueId != null)
                            || (ActiveIssueUtils.getActiveJiraIssue(project) != null && issueId != null
                            && !issueId.equals(ActiveIssueUtils.getActiveJiraIssue(project).getIssueKey()))) {
                        ActiveIssueUtils.activateIssue(project, null, issue, server, newDefaultList);
                    }
                } else {
                    addChangeListListener();
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            Messages.showInfoMessage(project, "Cannot activate issue " + finalActiveTaskUrl,
                                    PluginUtil.PRODUCT_NAME);
                        }
                    });

                }
            }
        }
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

        } catch (Exception e) {
            PluginUtil.getLogger().error(CANNOT_GET_ACTIVE_LOCAL_TASK_ISSUE_URL, e);
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

    public static boolean isValidIdeaVersion() {
        return IdeaVersionFacade.getInstance().isIdea9() && !IdeaVersionFacade.getInstance().isCommunityEdition();
    }
}