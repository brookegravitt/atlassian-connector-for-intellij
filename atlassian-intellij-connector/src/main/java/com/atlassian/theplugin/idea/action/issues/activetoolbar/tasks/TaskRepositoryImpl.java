package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.util.PluginUtil;

import java.lang.reflect.Method;

/**
 * User: pmaruszak
 */
public class TaskRepositoryImpl implements TaskRepository {
    private static final String TASK_REPOSITORY_CLASS = "com.intellij.tasks.TaskRepository";
    private final Object taskRepositoryObj;
    private Class taskRepositoryClass = null;


    public TaskRepositoryImpl(Object jiraRepositoryObj, ClassLoader classLoader) {
        this.taskRepositoryObj = jiraRepositoryObj;
        try {
            this.taskRepositoryClass = classLoader.loadClass(TASK_REPOSITORY_CLASS);
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot load " + TASK_REPOSITORY_CLASS + " class", e);
        }

    }


    public String getUrl() {
        if (taskRepositoryClass != null) {
            try {
                Method getUrl = null;
                getUrl = taskRepositoryClass.getMethod("getUrl");
                Object url = getUrl.invoke(taskRepositoryObj);
                return url.toString();
            } catch (Exception e) {
                PluginUtil.getLogger().error("Cannot get task repository url", e);
            }
        }
        return null;
    }

    public void setUrl(final String url) {
        if (taskRepositoryClass == null) {
            return;
        }        
        try {
            Method setUrl = null;
            setUrl = taskRepositoryClass.getMethod("setUrl", String.class);
            setUrl.invoke(taskRepositoryObj, url);
        } catch (Exception e) {
                PluginUtil.getLogger().error("Cannot set repository url", e);
        }        
    }

    public void setUsername(final String userName) {
        if (taskRepositoryClass == null) {
            return;
        }

        try {
            Method setUsername = null;
            setUsername = taskRepositoryClass.getMethod("setUsername", String.class);
            setUsername.invoke(taskRepositoryObj, userName);
        } catch (Exception e) {
                PluginUtil.getLogger().error("Cannot set username", e);
        }
    }

    public void setPassword(final String password) {
            if (taskRepositoryClass == null) {
            return;
        }
        try {
            Method setPassword = null;
            setPassword = taskRepositoryClass.getMethod("setPassword", String.class);
            setPassword.invoke(taskRepositoryObj, password);
        } catch (Exception e) {
                PluginUtil.getLogger().error("Cannot set password", e);
        }
    }

    public void setShared(final boolean shared) {
                if (taskRepositoryClass == null) {
            return;
        }
        try {
             Method setShared = taskRepositoryClass.getMethod("setShared", Boolean.TYPE);
             setShared.invoke(taskRepositoryObj, true);
        } catch (Exception e) {
                PluginUtil.getLogger().error("Cannot set password", e);
        }
    }

    public Object getTaskRepositoryObj() {
        return taskRepositoryObj;
    }
}
