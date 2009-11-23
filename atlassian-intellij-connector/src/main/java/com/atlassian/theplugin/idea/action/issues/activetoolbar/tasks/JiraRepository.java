package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.util.PluginUtil;

import java.lang.reflect.Method;

/**
 * User: pmaruszak
 */
public class JiraRepository extends TaskRepositoryImpl {
    private static final String JIRA_REPOSITORY_CLASS = "com.intellij.tasks.jira.JiraRepository";

    private final Object jiraRepositoryObj;
    private final ClassLoader classLoader;
    private Class jiraRepositoryClass = null;
    public JiraRepository(Object jiraRepositoryObj, ClassLoader classLoader) {
        super(jiraRepositoryObj, classLoader);

        this.jiraRepositoryObj = jiraRepositoryObj;
        this.classLoader = classLoader;
        try {
            jiraRepositoryClass = classLoader.loadClass(JIRA_REPOSITORY_CLASS);
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot load class", e);
        }

    }

    
    public LocalTask findTask(final String taskId) {
        Method findTask = null;
        try {
            findTask = jiraRepositoryClass.getMethod("findTask", String.class);
            Object foundTaskObj = findTask.invoke(jiraRepositoryObj, taskId);
            return new LocalTaskImpl(foundTaskObj, classLoader);
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot find task", e);
        }
        return null;
    }

    @Override
    public Object getTaskRepositoryObj() {
        return jiraRepositoryObj;
    }
}
