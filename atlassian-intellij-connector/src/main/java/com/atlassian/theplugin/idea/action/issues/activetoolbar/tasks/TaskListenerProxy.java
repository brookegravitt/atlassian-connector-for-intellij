package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import org.joda.time.DateTime;

import javax.swing.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author: pmaruszak
 */
public final class TaskListenerProxy implements InvocationHandler {
    public static final String TASK_LISTENER = "com.intellij.tasks.TaskListener";

    private static Method taskActivatedMethod;
    private static Class taskListenerInterface;
    private final ClassLoader classLoader;
    private final Project project;
    private Object prevObj;

    public static Object newInstance(final ClassLoader classLoader, final Project project) {

        try {
            taskListenerInterface = classLoader.loadClass(TASK_LISTENER);
            Object taskListenerObj = java.lang.reflect.Proxy.newProxyInstance(
                    classLoader,
                    new Class[]{taskListenerInterface},
                    new TaskListenerProxy(classLoader, project));
            taskActivatedMethod = taskListenerInterface.getMethod("taskActivated",
                    classLoader.loadClass(PluginTaskManager.LOCAL_TASK_CLASS));
            return taskListenerObj;
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot instantiate class " + TaskListenerProxy.class.getName());
        }

        return null;
    }


    private TaskListenerProxy(ClassLoader classLoader, Project project) {
        this.classLoader = classLoader;
        this.project = project;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
            throws Throwable {
        Object result;
        try {
            if (m.equals(taskActivatedMethod)) {
                if (isDifferentEvent(args[0])) {
                    final LocalTask lt = new LocalTaskImpl(args[0], classLoader);

                    if (!lt.isDefaultTask()) {
                        final ActiveJiraIssue jiraIssue = ActiveIssueUtils.getActiveJiraIssue(project);
                        if (jiraIssue == null || !lt.getId().equals(jiraIssue.getIssueKey())) {

                            final JiraServerData sd = PluginTaskManager.getInstance(project).findJiraPluginJiraServer(
                                    lt.getIssueUrl());

                            if (sd != null) {
                                final ActiveJiraIssue ai = new ActiveJiraIssueBean(sd.getServerId(), lt.getId(),
                                        new DateTime());
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        ActiveIssueUtils.activateIssue(project, null, ai, sd, null);
                                    }
                                });

                            }
                        }

                    } else {
                        SwingUtilities.invokeLater(new DeactivateIssueRunnable(project));
                    }
                }


            }
        } catch (Exception e) {
            throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
        }
        
        return null;
    }


    private synchronized boolean isDifferentIssue(LocalTask lt) {
        lt.getIssueUrl();
        ActiveJiraIssue ai = ActiveIssueUtils.getActiveJiraIssue(project);

        if (ai != null && lt != null && !ai.getIssueKey().equals(lt.getId()) || ai == null || lt == null) {
            return true;
        }
        return false;

    }
    private synchronized boolean isDifferentEvent(Object newObject) {
        boolean isDifferent =   prevObj == null || prevObj != null && !prevObj.equals(newObject);
        prevObj = newObject;
        return isDifferent;
    }
}
