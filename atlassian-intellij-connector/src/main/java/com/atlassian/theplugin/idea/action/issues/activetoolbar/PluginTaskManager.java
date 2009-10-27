package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.cache.RecentlyOpenIssuesCache;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListAdapter;
import com.intellij.openapi.vcs.changes.ChangeListListener;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.tasks.*;
import com.intellij.tasks.impl.LocalTaskImpl;
import com.intellij.tasks.impl.TaskManagerImpl;
import com.intellij.tasks.jira.JiraRepository;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 * Date: Oct 22, 2009
 */
public final class PluginTaskManager {
    private static Map<Project, PluginTaskManager> managers = new HashMap<Project, PluginTaskManager>();
    private final Project project;
    private ChangeListListener myListener;
    private ClassLoader classLoader;
    private Class taskManagerClass;
    private Object taskManagerObj;


    private PluginTaskManager(final Project Project) {
        project = Project;
        classLoader = getTaskManagerDescriptor().getPluginClassLoader();
        try {
            taskManagerClass = classLoader.loadClass(TaskManagerImpl.class.getName());
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot get task class loader", e);
        }

        taskManagerObj = getTaskManager();
        myListener = new LocalChangeListAdapter();

    }

    ;

    public static PluginTaskManager getInstance(final Project project) {
        if (!managers.containsKey(project)) {
            managers.put(project, new PluginTaskManager(project));
        }

        return managers.get(project);
    }

    public synchronized void addChangeListListener() {
        ChangeListManager.getInstance(project).addChangeListListener(myListener);
    }

    public synchronized void removeChangeListListener() {
            ChangeListManager.getInstance(project).removeChangeListListener(myListener);
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
                PluginUtil.getLogger().error("Cannot load class:" + TaskManager.class.getName(), e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot load class:" + TaskManager.class.getName(), e);
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot load class:" + TaskManager.class.getName(), e);
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
    public Object findLocalTaskById(String taskId) {
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
    public Object findLocalTaskBySummary(String summary) {
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
    public Object createLocalTask(final String taskId, ServerData server) {

        if (classLoader != null) {
            try {
                Object jiraRepository = getJiraRepository(server);

                if (jiraRepository != null) {
                    Class jiraRepositoryClass = classLoader.loadClass(JiraRepository.class.getName());
                    Method findTask = jiraRepositoryClass.getMethod("findTask", String.class);
                    Object task = findTask.invoke(jiraRepository, taskId);
                    return task;
                }
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot create local task:" + TaskManager.class.getName(), e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot create local task:" + TaskManager.class.getName(), e);
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot create local task:" + TaskManager.class.getName(), e);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot create local task:" + TaskManager.class.getName(), e);
            }

        }
        return null;
    }

    public String getTaskId(final Object task) {
        if (classLoader != null) {
            try {

                Class localTaskClass = classLoader.loadClass(LocalTaskImpl.class.getName());
                Method getTaskId = localTaskClass.getMethod("getId");

                Object localTaskObj = getTaskId.invoke(task);
                return localTaskObj.toString();
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot get task id:" + LocalTask.class.getName(), e);
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot get task id:" + LocalTask.class.getName(), e);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot get task id:" + LocalTask.class.getName(), e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot get task id:" + LocalTask.class.getName(), e);
            }
        }

        return null;
    }

     public String getTaskSummary(final Object task) {
        if (classLoader != null) {
            try {

                Class localTaskClass = classLoader.loadClass(LocalTaskImpl.class.getName());
                Method getSummaryMethod = localTaskClass.getMethod("getSummary");

                Object localTaskObj = getSummaryMethod.invoke(task);
                return localTaskObj.toString();
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot get task summary:" + LocalTask.class.getName(), e);
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot get task summary:" + LocalTask.class.getName(), e);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot get task summary:" + LocalTask.class.getName(), e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot get task summary:" + LocalTask.class.getName(), e);
            }
        }

        return null;
    }
    public void activateTask(final Object task, boolean clearContext, boolean createChangeset) {
        if (classLoader != null && task != null) {
            try {

                Class taskClass = classLoader.loadClass(com.intellij.tasks.Task.class.getName());

                if (taskManagerObj != null) {
                    Method activateLocalTask = taskManagerClass.getMethod("activateTask", taskClass, Boolean.TYPE, Boolean.TYPE);
                    activateLocalTask.invoke(taskManagerObj, task, clearContext, createChangeset);
                }
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot activate local task:" + LocalTask.class.getName(), e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot activate local task:" + LocalTask.class.getName(), e);
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot activate local task:" + LocalTask.class.getName(), e);
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot activate local task:" + LocalTask.class.getName(), e);
            }
        }
    }

    //todo fix this to shitch to task that is linked to Default changeset
    public void deactivateToDefaultTask() {
        Object defaultTaskObj = findLocalTaskBySummary("Default");
        if (defaultTaskObj != null) {
            activateTask(defaultTaskObj, false, false);
        }
    }
    
    //get or creates
    private Object getJiraRepository(final ServerData jiraServer) {

        if (classLoader != null) {
            try {
                taskManagerClass = classLoader.loadClass(TaskManagerImpl.class.getName());

                if (taskManagerObj != null) {
                    //@todo we should check repository type and username password not to duplicate
                    Method getAllRepositoryTypes = taskManagerClass.getMethod("getAllRepositoryTypes");
                    Class taskRepositoryTypeClass = classLoader.loadClass(TaskRepositoryType.class.getName());
                    Class taskRepositoryClass = classLoader.loadClass(TaskRepository.class.getName());

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
                            Class jiraRepositoryClass = classLoader.loadClass(JiraRepository.class.getName());

                            Method setUrl = jiraRepositoryClass.getMethod("setUrl", String.class);
                            setUrl.invoke(newRepository, jiraServer.getUrl());

                            Method setUsername = jiraRepositoryClass.getMethod("setUsername", String.class);
                            setUsername.invoke(newRepository, jiraServer.getUsername());

                            Method setPassword = jiraRepositoryClass.getMethod("setPassword", String.class);
                            setPassword.invoke(newRepository, jiraServer.getPassword());
                            jiraRepos.add(newRepository);

                            Method setRepositories = taskManagerClass.getMethod("setRepositories", List.class, TaskRepositoryType.class);
                            setRepositories.invoke(taskManagerObj, jiraRepos, repoType);
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
            }
        }
        return null;
    }


    private void addChangeListListener(final Project project) {
        ChangeListManager manager = ChangeListManager.getInstance(project);
        manager.addChangeListListener(new ChangeListAdapter() {

            @Override
            public void defaultListChanged(ChangeList oldDefaultList, ChangeList newDefaultList) {
                String activeTaskUrl = getActiveTaskUrl(project);
                if (activeTaskUrl != null) {

                }
            }
        });
    }

    private String getActiveTaskUrl(final Project project) {
        try {
            if (classLoader != null) {
                Class localTaskClass = classLoader.loadClass(com.intellij.tasks.LocalTask.class.getName());
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
            PluginUtil.getLogger().error("Cannot get active local task issue url:" + LocalTask.class.getName(), e);
        } catch (NoSuchMethodException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url:" + LocalTask.class.getName(), e);
        } catch (IllegalAccessException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url:" + LocalTask.class.getName(), e);
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url:" + LocalTask.class.getName(), e);
        }

        return null;
    }

    private class LocalChangeListAdapter extends ChangeListAdapter {
          @Override
            public void defaultListChanged(ChangeList oldDefaultList, ChangeList newDefaultList) {
                String activeTaskUrl = getActiveTaskUrl(project);
                if (activeTaskUrl != null) {
                    final JiraServerData server = findJiraPluginJiraServer(activeTaskUrl);
//                    JiraIssueAdapter issueAdapter = findActiveJiraIssue(activeTaskUrl);
                    String issueId = getActiveTaskId();
                    final ActiveJiraIssueBean issue = new ActiveJiraIssueBean();
                    issue.setIssueKey(issueId);
                    issue.setServerId(server != null ? (ServerIdImpl) server.getServerId() : null);

                    if ((ActiveIssueUtils.getActiveJiraIssue(project) == null && issueId != null)
                         || (ActiveIssueUtils.getActiveJiraIssue(project) != null && issueId != null  
                            && !issueId.equals(ActiveIssueUtils.getActiveJiraIssue(project).getIssueKey())))
                      ApplicationManager.getApplication().invokeLater(new Runnable() {
			            public void run() {
                            ActiveIssueUtils.activateIssue(project, null, issue, server);
			            }
		              }, ModalityState.defaultModalityState());
                }


            }
    }

    private JiraIssueAdapter findActiveJiraIssue(String issueUrl) {
        RecentlyOpenIssuesCache cache = IdeaHelper.getProjectComponent(project, RecentlyOpenIssuesCache.class);
        if (cache != null) {
            for (JiraIssueAdapter issue :  cache.getLoadedRecenltyOpenIssues()) {
                if (issueUrl.equals(issue.getIssueUrl())) {
                    return issue;
                }
            }
        }
        return null;
    }
    public String getActiveTaskId() {
        try {
            if (classLoader != null) {
                Class localTaskClass = classLoader.loadClass(com.intellij.tasks.LocalTask.class.getName());
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
            PluginUtil.getLogger().error("Cannot get active local task issue url:" + LocalTask.class.getName(), e);
        } catch (NoSuchMethodException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url:" + LocalTask.class.getName(), e);
        } catch (IllegalAccessException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url:" + LocalTask.class.getName(), e);
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot get active local task issue url:" + LocalTask.class.getName(), e);
        }

        return null;
    };

    @Nullable
    private JiraServerData findJiraPluginJiraServer(String issueUrl) {
       for (JiraServerData server : IdeaHelper.getProjectCfgManager(project).getAllEnabledJiraServerss()) {
           if (issueUrl.contains(server.getUrl())) {
               return server;
           }
       }

       return null;
    }
}
