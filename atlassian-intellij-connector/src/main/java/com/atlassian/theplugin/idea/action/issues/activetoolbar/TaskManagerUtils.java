package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.impl.LocalTaskImpl;
import com.intellij.tasks.impl.TaskManagerImpl;
import com.intellij.tasks.jira.JiraRepository;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Oct 22, 2009
 * Time: 1:04:09 PM
 * To change this template use File | Settings | File Templates.
 */
public final class TaskManagerUtils {
    private TaskManagerUtils() {

    }

    ;

    public static Object[] getLocalTasks(final Project project) {
        for (IdeaPluginDescriptor descriptor : ApplicationManager.getApplication().getPlugins()) {
            if (descriptor.getPluginId().getIdString().equals("com.intellij.tasks")) {
                try {

                    //Object taskManager =  descriptor.getPluginClassLoader().loadClass(TaskManager.class.getName()).getMethods()[0].invoke(null, project);
                    Class taskManagerClass = descriptor.getPluginClassLoader().loadClass(TaskManager.class.getName());


                    Method getManager = taskManagerClass.getMethod("getManager", Project.class);

                    Object taskManager = getManager.invoke(null, project);
                    Method getLocalTasks = taskManagerClass.getMethod("getLocalTasks");
                    Object[] localTasksObj = (Object[]) getLocalTasks.invoke(taskManager);

                    return localTasksObj;
                } catch (ClassNotFoundException e) {
                    PluginUtil.getLogger().error("Cannot get local tasks ", e);
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


    private static Object getTaskManager(final Project project) {

        IdeaPluginDescriptor descriptor = getTaskManagerDescriptor(project);
        if (descriptor != null) {
            try {
                Class taskManagerClass = descriptor.getPluginClassLoader().loadClass(TaskManagerImpl.class.getName());
                Method getManager = taskManagerClass.getMethod("getManager", Project.class);
                Object taskManager = getManager.invoke(null, project);
                return taskManager;
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot load class:" + TaskManager.class.getName(), e);
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

    private static IdeaPluginDescriptor getTaskManagerDescriptor(final Project project) {
        for (IdeaPluginDescriptor descriptor : ApplicationManager.getApplication().getPlugins()) {
            if (descriptor.getPluginId().getIdString().equals("com.intellij.tasks")) {
                return descriptor;
            }
        }
        return null;
    }


    public static Object findLocalTask(Project project, String taskId) {
        Object[] localTasks = TaskManagerUtils.getLocalTasks(project);
        if (localTasks != null) {
            Object foundTask = null;
            for (Object t : localTasks) {
                String localTaskId = TaskManagerUtils.getTaskId(project, t);
                if (localTaskId == null || !localTaskId.equals(taskId)) {
                    continue;
                }
                foundTask = t;
                break;
            }
        }
        return null;
    }

    public static Object createLocalTask(final Project project, final String taskId, ServerData server) {

        try {
            IdeaPluginDescriptor descriptor = getTaskManagerDescriptor(project);

            Object jiraRepository = getJiraRepository(project, server);


            if (jiraRepository != null) {
                Class jiraRepositoryClass = descriptor.getPluginClassLoader().loadClass(JiraRepository.class.getName());
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


        return null;
    }

    public static String getTaskId(final Project project, final Object task) {
        try {
            IdeaPluginDescriptor descriptor = getTaskManagerDescriptor(project);
            Class localTaskClass = descriptor.getPluginClassLoader().loadClass(LocalTaskImpl.class.getName());
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

        return null;
    }

    public static void activateTask(final Project project, final Object task, boolean clearContext, boolean createChangeset) {
        try {
            Object taskManager = getTaskManager(project);
            IdeaPluginDescriptor descriptor = getTaskManagerDescriptor(project);
            Class taskManagerClass = descriptor.getPluginClassLoader().loadClass(TaskManagerImpl.class.getName());
            Class taskClass = descriptor.getPluginClassLoader().loadClass(com.intellij.tasks.Task.class.getName());

            if (taskManager != null) {
                Method activateLocalTask = taskManagerClass.getMethod("activateTask", taskClass, Boolean.TYPE, Boolean.TYPE);
                Object localTaskObj = activateLocalTask.invoke(taskManager, task, clearContext, createChangeset);
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


    //get or creates
    public static Object getJiraRepository(final Project project, final ServerData jiraServer) {
        Object taskManager = getTaskManager(project);
        IdeaPluginDescriptor descriptor = getTaskManagerDescriptor(project);
        Class taskManagerClass = null;
        try {
            taskManagerClass = descriptor.getPluginClassLoader().loadClass(TaskManagerImpl.class.getName());

            if (taskManager != null) {
                //@todo we should check repository type and username password not to duplicate
                Method getAllRepositoryTypes = taskManagerClass.getMethod("getAllRepositoryTypes");
                Class taskRepositoryTypeClass = descriptor.getPluginClassLoader().loadClass(TaskRepositoryType.class.getName());
                Class taskRepositoryClass = descriptor.getPluginClassLoader().loadClass(TaskRepository.class.getName());

                Object[] repoTypes = (Object[]) getAllRepositoryTypes.invoke(taskManager);


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
                        Class jiraRepositoryClass = descriptor.getPluginClassLoader().loadClass(JiraRepository.class.getName());
                        
                        Method setUrl = jiraRepositoryClass.getMethod("setUrl", String.class);
                        setUrl.invoke(newRepository, jiraServer.getUrl());

                        Method setUsername = jiraRepositoryClass.getMethod("setUsername", String.class);
                        setUsername.invoke(newRepository, jiraServer.getUsername());

                        Method setPassword = jiraRepositoryClass.getMethod("setPassword", String.class);
                        setPassword.invoke(newRepository, jiraServer.getPassword());
                        jiraRepos.add(newRepository);

                        Method setRepositories = taskManagerClass.getMethod("setRepositories", List.class, TaskRepositoryType.class);
                        setRepositories.invoke(taskManager, jiraRepos, repoType);
                         return newRepository;
                    }
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }


//    private static void addJiraRepository(Project project, Object jiraRepository) {
//        Object taskManager = getTaskManager(project);
//        IdeaPluginDescriptor descriptor = getTaskManagerDescriptor(project);
//        Class taskManagerClass = null;
//        try {
//            taskManagerClass = descriptor.getPluginClassLoader().loadClass(TaskManagerImpl.class.getName());
//
//            if (taskManager != null) {
//                //@todo we should check repository type and username password not to duplicate
//                Method getAllRepositories = taskManagerClass.getMethod("getAllRepositories");
//                Class taskRepositoryClass = descriptor.getPluginClassLoader().loadClass(TaskRepository.class.getName());
//                Object[] repos = (Object[]) getAllRepositories.invoke(taskManager);
//                Object list
//
//
//                Method setRepositories = taskRepositoryClass.getMethod("setRepositories", Array.class);
//
//                return newRepo;
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (InstantiationException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
//        return null;
//    }
}
