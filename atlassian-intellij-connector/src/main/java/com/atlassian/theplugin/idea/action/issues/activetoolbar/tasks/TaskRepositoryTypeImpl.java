package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.util.PluginUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * User: pmaruszak
 */
public class TaskRepositoryTypeImpl implements TaskRepositoryType {
    private static final String TASK_REPOSITORY_TYPE_CLASS = "com.intellij.tasks.TaskRepositoryType";
    private Object repositoryTypeObj;
    private final ClassLoader classLoader;
    private Class taskRepositoryTypeClass;

    public TaskRepositoryTypeImpl(Object repositoryTypeObj, ClassLoader classLoader) {
        this.repositoryTypeObj = repositoryTypeObj;
        this.classLoader = classLoader;
        try {
            taskRepositoryTypeClass = classLoader.loadClass(TASK_REPOSITORY_TYPE_CLASS);
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot load class " + TASK_REPOSITORY_TYPE_CLASS);
        }
    }

    
    @Nullable
    public String getName() {
        if (taskRepositoryTypeClass != null) {
            try {
                Method getName = null;
                getName = taskRepositoryTypeClass.getMethod("getName");
                return (String) getName.invoke(repositoryTypeObj);
            } catch (Exception e) {
                PluginUtil.getLogger().error("Cannot get repository type name");
            }
        }
        return null;
    }

    public List<TaskRepository> getRepositories() {
        List<TaskRepository> repositories = new ArrayList<TaskRepository>();

        if (taskRepositoryTypeClass == null) {
            return repositories;
        }

        try {
            Method getRepositories = null;
            getRepositories = taskRepositoryTypeClass.getMethod("getRepositories");
            List jiraRepos = (List) getRepositories.invoke(repositoryTypeObj);
            for (Object r : jiraRepos) {
                repositories.add(new TaskRepositoryImpl(r, classLoader));
            }
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot get repositories", e);
        }

        return repositories;
    }

    @Nullable
    public TaskRepository createRepository() {
        if (taskRepositoryTypeClass == null) {
            return null;
        }

        try {
            Method createRepository = null;
            createRepository = taskRepositoryTypeClass.getMethod("createRepository");
            return new JiraRepository(createRepository.invoke(repositoryTypeObj), classLoader);
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot create repository", e);
        }

        return null;
    }

   public Object getRepositoryTypeObj() {
       return repositoryTypeObj;
   }

   public Class getTaskRepositoryTypeClass() {
       return taskRepositoryTypeClass;
   }
}
