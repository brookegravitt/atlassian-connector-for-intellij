package com.atlassian.connector.intellij.tasks;

import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks.DeactivateIssueRunnable;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.LocalTask;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.swing.*;
import java.awt.*;

/**
 * @author pmaruszak
 * @date Feb 2, 2010
 */
public class TaskListenerImpl implements com.intellij.tasks.TaskListener {
    volatile LocalTask prevTask = null;
    volatile DateTime prevTime = new DateTime();
    private final Project project;
    private final PluginTaskManager pluginTaskManager;
    volatile private boolean initialized = false;

    public TaskListenerImpl(final Project project, PluginTaskManager pluginTaskManager) {
        this.project = project;
        this.pluginTaskManager = pluginTaskManager;
    }

    public void taskActivated(final LocalTask localTask) {
        System.out.println("Activated " + localTask.getIssueUrl());
        synchronized (this) {
            EventQueue.invokeLater(new ActivateRunnable(localTask, prevTask, prevTime, initialized));
            prevTask = localTask;
            prevTime = new DateTime();
            initialized = true;
        }

    }


    class ActivateRunnable implements Runnable {
        private final LocalTask localTask;
        private final LocalTask prevTask;
        private final DateTime prevTime;
        private final boolean initialized;

        public ActivateRunnable(LocalTask localTask, LocalTask prevTask, DateTime prevTime, boolean initialized) {

            this.localTask = localTask;
            this.prevTask = prevTask;
            this.prevTime = prevTime;
            this.initialized = initialized;
        }

        public void run() {
            Period period = new Period(prevTime, new DateTime());

            if (prevTask != localTask || period.getMillis() > 500 || !initialized) {
                if (!PluginTaskManager.isDefaultTask(localTask)) {
                    final ActiveJiraIssue jiraIssue = ActiveIssueUtils.getActiveJiraIssue(project);
                    if (jiraIssue == null || !localTask.getId().equals(jiraIssue.getIssueKey())) {

                        final JiraServerData sd = pluginTaskManager.findJiraPluginJiraServer(localTask.getIssueUrl());

                        if (sd != null) {
                            final ActiveJiraIssue ai = new ActiveJiraIssueBean(sd.getServerId(), localTask.getIssueUrl(), localTask.getId(),
                                    new DateTime());

                            ActiveIssueUtils.activateIssue(project, null, ai, sd, null);
                        }
                    }

                } else {
                    SwingUtilities.invokeLater(new DeactivateIssueRunnable(project));
                }


            }
        }
    }
}
