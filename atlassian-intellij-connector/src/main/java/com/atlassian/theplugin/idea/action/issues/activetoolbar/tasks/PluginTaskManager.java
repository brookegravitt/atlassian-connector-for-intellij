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

package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.jira.ActiveIssueResultHandler;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
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
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListListener;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * User: pmaruszak
 * Date: Oct 22, 2009
 */
public final class PluginTaskManager {
    private static final String TASK_MANAGER_IMPL_CLASS = "com.intellij.tasks.impl.TaskManagerImpl";

    private static final String TASK_CLASS = "com.intellij.tasks.Task";
    public static final String LOCAL_TASK_CLASS = "com.intellij.tasks.LocalTask";
    private static final String CHANGE_LIST_INFO = "com.intellij.tasks.ChangeListInfo";
    private static final String PLUGIN_ID_TASKS = "com.intellij.tasks";

    private static final String CANNOT_GET_ACTIVE_LOCAL_TASK_ISSUE_URL = "Cannot get active local task issue url";
    private static final String CANNOT_GET_JIRA_REPOSITORY = "Cannot get JIRA repository";
    private static final String CANNOT_ACTIVATE_LOCAL_TASK = "Cannot activate local task";
    private static final String CANNOT_GET_LOCAL_TASK_ASSOCIATED_CHANGE_LIST = "Cannot get local task associated change list";

    private static final String CANNOT_LOAD_CLASS_TASK_MANAGER = "Cannot load class TaskManager";
    private static final String CANNOT_GET_LOCAL_TASKS = "Cannot get local tasks";
    private static final String CANNOT_GET_ASSOCIATED_LOCAL_TASK_ISSUE_URL =
            "Cannot get task associated with active change list";
    private static final String CANNOT_REMOVE_LOCAL_TASK = "Cannot remove local task from IDEA";

    private static Map<Project, PluginTaskManager> managers = new HashMap<Project, PluginTaskManager>();
    private final Project project;
    private ChangeListListener myListener;
    private ClassLoader classLoader;
    private Class taskManagerClass;
    private Object taskManagerObj;
    private boolean alreadyAdded = false;
    private static boolean actionsRegistered = false;
    private Object taskListnerObj;


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
            myListener = new TaskChangeListAdapter(project);
            taskListnerObj = TaskListenerProxy.newInstance(classLoader, this);
            if (taskListnerObj != null) {
                addTaskListener(taskListnerObj);
            }
        }
    }

    public static PluginTaskManager getInstance(final Project project) {
        if (!managers.containsKey(project)) {
            managers.put(project, new PluginTaskManager(project));
        }

        return managers.get(project);
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

    public void addTaskListener(Object taskListenerObj) {
        try {
        Method addTaskListenerMethod = taskManagerClass.getMethod("addTaskListener",
                classLoader.loadClass(TaskListenerProxy.TASK_LISTENER));
            addTaskListenerMethod.invoke(taskManagerObj, taskListenerObj);
        } catch (Exception e) {
            PluginUtil.getLogger().error("TaskListener not added:" + e.getMessage());
        }
    }
    public List<TaskRepository> getAllRepositories() {
        Method getAllRepositoriesMethod = null;
        List<TaskRepository> repos = new ArrayList<TaskRepository>();
        try {
            getAllRepositoriesMethod = taskManagerClass.getMethod("getAllRepositories");
            Object[] repositoriesArrayObj = (Object[]) getAllRepositoriesMethod.invoke(taskManagerObj);

            for (Object repo : repositoriesArrayObj) {
                repos.add(new TaskRepositoryImpl(repo, classLoader));
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return repos;
    }

    public void removeTaskFromIdea(LocalTask localTask) {
        if (!isValidIdeaVersion()) {
            return;
        }
        //com.intellij.tasks.LocalTask
        if (classLoader != null && localTask != null) {
            try {
                Class localTaskClass = classLoader.loadClass(LOCAL_TASK_CLASS);
                Method removeTaskMethod = taskManagerClass.getMethod("removeTask", localTaskClass);
                removeTaskMethod.invoke(taskManagerObj, localTask.getLocalTaskObj());

            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_REMOVE_LOCAL_TASK, e);
            }
        }

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

        LocalTask defaultTask = getDefaultTask();
        Object defaultTaskObj = defaultTask != null ? defaultTask.getLocalTaskObj() : null;
        if (defaultTask != null) {
            activateTask(defaultTask, false, false);
        }

        addChangeListListener();
    }


    @Nullable
    String getActiveIssueUrl(String issueKey) {
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
        LocalTask foundTask;

        ServerData server = IdeaHelper.getProjectCfgManager(project).getServerr(issue.getServerId());
        foundTask = findLocalTaskByUrl(getActiveIssueUrl(issue.getIssueKey()));
        if (foundTask == null) {
            foundTask = findLocalTaskById(issue.getIssueKey());
        }

        //ADD or GET JiraRepository
        JiraRepository jiraRepository = getJiraRepository(server);
        if (foundTask != null) {
            LocalTask activeTask = getActiveTask();
            if (activeTask == null || (activeTask.getLocalTaskObj() != foundTask.getLocalTaskObj())) {
                activateTask(foundTask, false, true);
            }
        } else {
            //todo search for issue ID and modify task insead of creating one
            foundTask = jiraRepository != null ? jiraRepository.findTask(issue.getIssueKey()) : null;
            if (foundTask != null) {
                activateTask(foundTask, true, true);
            }
        }
    }

    private LocalTask getChangeListTask(ChangeList changeList) {
        List<LocalTask> localTasks = getLocalTasks();
        if (localTasks != null) {
            for (LocalTask t : localTasks) {
                String changeListId = getChangeListId(t);
                if (changeListId != null && changeListId.equals(getLocalChangeListId(changeList))) {
                    return t;
                }
            }
        }
        return null;
    }

    @Nullable
    String getLocalChangeListId(ChangeList localList) {
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
    private LocalTask getDefaultTask() {
        ChangeListManager manager = ChangeListManager.getInstance(project);
        if (manager != null) {
            ChangeList defaultChangeList = getDefaultChangeList();
            return getChangeListTask(defaultChangeList);
        }

        return null;
    }


    //assume that RO change list is default

    @Nullable
    LocalChangeList getDefaultChangeList() {
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

    private List<LocalTask> getLocalTasks() {

        try {

            Method getManager = taskManagerClass.getMethod("getManager", Project.class);

            Object taskManager = getManager.invoke(null, project);
            Method getLocalTasks = taskManagerClass.getMethod("getLocalTasks");
            List<LocalTask> localTasks = new ArrayList<LocalTask>();
            Object[] localTasksObj = (Object[]) getLocalTasks.invoke(taskManager);
            if (localTasksObj != null) {
                for (Object t : localTasksObj) {
                    localTasks.add(new LocalTaskImpl(t, classLoader));
                }
            }
            return localTasks;
        } catch (Exception e) {
            PluginUtil.getLogger().error(CANNOT_GET_LOCAL_TASKS, e);
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
    private LocalTask findLocalTaskById(String taskId) {
        List<LocalTask> localTasks = getLocalTasks();
        if (localTasks != null) {
            for (LocalTask task : localTasks) {
                if (task.getId() != null && task.getId().equals(taskId)) {
                    return task;
                }
            }
        }
        return null;
    }

    @Nullable
    private LocalTask findLocalTaskByUrl(String url) {
        List<LocalTask> localTasks = getLocalTasks();
        if (localTasks != null) {
            for (LocalTask task : localTasks) {
                if (task.getIssueUrl() != null && task.getIssueUrl().equals(url)) {
                    return task;
                }
            }
        }
        return null;
    }


    @Nullable
    private String getChangeListId(final LocalTask task) {
        if (classLoader != null) {
            try {

                Class changeListInfoClass = classLoader.loadClass(CHANGE_LIST_INFO);
                Field id = changeListInfoClass.getField("id");

                List<Object> changeSetList = task.getChangeLists();
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

    private void activateTask(final LocalTask task, final boolean clearContext, final boolean createChangeset) {
        if (classLoader != null && task != null) {
            try {

                Class taskClass = classLoader.loadClass(TASK_CLASS);

                if (taskManagerObj != null) {
                    final Method activateLocalTask = taskManagerClass.getMethod("activateTask", taskClass,
                            Boolean.TYPE, Boolean.TYPE);
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            try {
                                activateLocalTask.invoke(taskManagerObj, task.getLocalTaskObj(), clearContext, createChangeset);
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


//    private List<TaskRepositoryType> getAllRepositoryTypes() {
//        List<TaskRepositoryType> repoTypes = new ArrayList<TaskRepositoryType>();
//        try {
//            Method getAllRepositoryTypes = taskManagerClass.getMethod("getAllRepositoryTypes");
//            Object[] repoTypesObj = (Object[]) getAllRepositoryTypes.invoke(taskManagerObj);
//            if (repoTypesObj != null) {
//                for (Object r : repoTypesObj) {
//                    repoTypes.add(new JiraTaskRepositoryTypeImpl(r, classLoader));
//                }
//            }
//        } catch (Exception e) {
//            PluginUtil.getLogger().error("Cannot get all repository types", e);
//        }
//
//        return repoTypes;
//    }

    private void setRepositories(Object repositories) {

        try {
            Method setRepositories = null;
            setRepositories = taskManagerClass.getMethod("setRepositories", List.class);
            //public abstract void (java.util.List list, com.intellij.tasks.TaskRepositoryType taskrepositorytype);
            setRepositories.invoke(taskManagerObj, repositories);
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot set repositories for repository type JIRA", e);
        }

    }

//    //get or creates
//    private JiraRepository getJiraRepository(final ServerData jiraServer) {
//
//        if (classLoader != null) {
//            try {
//                taskManagerClass = classLoader.loadClass(TASK_MANAGER_IMPL_CLASS);
//
//                if (taskManagerObj != null) {
//                    //@todo we should check repository type and username password not to duplicate
//                    for (TaskRepositoryType repositoryType : getAllRepositoryTypes()) {
//
//                        if (repositoryType != null && repositoryType.getName() != null
//                                && repositoryType.getName().equalsIgnoreCase("JIRA")) {
//                            List<TaskRepository> repositories = repositoryType.getRepositories();
//
//                            for (TaskRepository jiraRepo : repositories) {
//
//                                String url = jiraRepo.getUrl();
//                                if (url != null && url.equalsIgnoreCase(jiraServer.getUrl())) {
//                                    return (JiraRepository) jiraRepo;
//                                }
//                            }
//
//                            //create jira repo
//                            JiraRepository jiraRepository = (JiraRepository) repositoryType.createRepository();
//                            if (jiraRepository != null) {
//                                jiraRepository.setUrl(jiraServer.getUrl());
//                                jiraRepository.setUsername(jiraServer.getUsername());
//                                jiraRepository.setPassword(jiraServer.getPassword());
//                                jiraRepository.setShared(true);
//                            }
//
//                            Class listClass = classLoader.loadClass("java.util.ArrayList");
//                            List<Object> newJiraReposList = (List<Object>) listClass.newInstance();
//                            for (TaskRepository repo : repositories) {
//                                newJiraReposList.add(repo.getTaskRepositoryObj());
//                            }
//
//                            newJiraReposList.add(jiraRepository.getTaskRepositoryObj());
//
//                            setRepositories(newJiraReposList, repositoryType);
//                            return jiraRepository;
//                        }
//                    }
//                }
//
//            } catch (Exception e) {
//                PluginUtil.getLogger().error(CANNOT_GET_JIRA_REPOSITORY, e);
//            }
//        }
//        return null;
//    }

    //get or creates

    @Nullable
    private JiraRepository getJiraRepository(final ServerData jiraServer) {

        if (classLoader != null) {
            try {
                taskManagerClass = classLoader.loadClass(TASK_MANAGER_IMPL_CLASS);

                if (taskManagerObj != null) {
                    Collection<TaskRepository> repositories = getAllRepositories();
                    //@todo we should check repository type and username password not to duplicate
                    if (repositories == null) {
                        return null;
                    }

                    for (TaskRepository repository : repositories) {
                        TaskRepositoryType repositoryType = repository.getRepositoryType();

                        if (repositoryType != null && repositoryType.getName() != null
                                && repositoryType.getName().equalsIgnoreCase("JIRA")) {

                            String url = repository.getUrl();
                            if (url != null && url.equalsIgnoreCase(jiraServer.getUrl())) {
                                return new JiraRepository(repository.getTaskRepositoryObj(), classLoader);
                            }
                        }
                    }
                    //create jira repo

                }


            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_GET_JIRA_REPOSITORY, e);
            }
        }
        return createJiraRepository(jiraServer);
    }

    @Nullable
    private JiraRepository createJiraRepository(final ServerData jiraServer) {
        JiraTaskRepositoryTypeImpl rt =  JiraTaskRepositoryTypeImpl.createInstance(classLoader);
        try {
            JiraRepository jiraRepository = (JiraRepository) rt.createRepository();
            if (jiraRepository != null) {
                jiraRepository.setUrl(jiraServer.getUrl());
                jiraRepository.setUsername(jiraServer.getUsername());
                jiraRepository.setPassword(jiraServer.getPassword());
                jiraRepository.setShared(true);
            }

            Class listClass = classLoader.loadClass("java.util.ArrayList");
            List<Object> newJiraReposList = (List<Object>) listClass.newInstance();
            List<TaskRepository> repositories = getAllRepositories();

            for (TaskRepository repo : repositories) {
                newJiraReposList.add(repo.getTaskRepositoryObj());
            }

            newJiraReposList.add(jiraRepository.getTaskRepositoryObj());

            setRepositories(newJiraReposList);
            return jiraRepository;
        } catch (Exception e) {

        }

        return null;
    }

    void deactivateTask() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            private final JiraWorkspaceConfiguration conf =
                    IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);

            public void run() {

                ActiveIssueUtils.deactivate(project, new ActiveIssueResultHandler() {
                    public void success() {
                        if (conf != null) {
                            conf.setActiveJiraIssuee(null);
                            PluginTaskManager.getInstance(project).addChangeListListener();
                        }
                    }

                    public void failure(Throwable problem) {

                        if (conf != null) {
                            PluginTaskManager.getInstance(project).activateLocalTask(conf.getActiveJiraIssuee());
                            PluginTaskManager.getInstance(project).addChangeListListener();
                        }
                    }

                    public void cancel(String problem) {
                        if (conf != null) {
                            PluginTaskManager.getInstance(project).activateLocalTask(conf.getActiveJiraIssuee());
                            PluginTaskManager.getInstance(project).addChangeListListener();
                        }
                    }
                });
            }
        }, ModalityState.defaultModalityState());
    }

    LocalTask getActiveTask() {
        try {
            if (classLoader != null) {
                if (taskManagerObj != null) {
                    Method getActiveTaskMethod = taskManagerClass.getMethod("getActiveTask");
                    return new LocalTaskImpl(getActiveTaskMethod.invoke(taskManagerObj), classLoader);
                }
            }

        } catch (Exception e) {
            PluginUtil.getLogger().error(CANNOT_GET_ACTIVE_LOCAL_TASK_ISSUE_URL, e);
        }

        return null;
    }

    @Nullable
    JiraServerData findJiraPluginJiraServer(String issueUrl) {
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

    @Nullable
    LocalTask findTaskByChangeList(ChangeList newDefaultList) {
        try {
            Class changeListInfoClass = classLoader.loadClass(CHANGE_LIST_INFO);
            Constructor[] constructors = changeListInfoClass.getConstructors();
            Object changeListInfoObject;

            if (constructors != null && constructors.length > 0) {
                changeListInfoObject = constructors[1].newInstance(newDefaultList);
                if (changeListInfoObject != null) {

                    List<LocalTask> localTasks = getLocalTasks();
                    for (LocalTask localTask : localTasks) {
                        List<Object> taskChangeLists = localTask.getChangeLists();
                        if (taskChangeLists != null && taskChangeLists.contains(changeListInfoObject)) {
                            return localTask;
                        }
                    }
                }
            }

        } catch (Exception e) {
            PluginUtil.getLogger().error(CANNOT_GET_ASSOCIATED_LOCAL_TASK_ISSUE_URL);
        }
        return null;
    }
}