package com.atlassian.connector.intellij.tasks;

import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks.DeactivateIssueRunnable;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.LocalTask;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import javax.swing.*;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author pmaruszak
 * @date Feb 5, 2010
 */
public class ActivationProcessingManager implements ProjectComponent {
    private LinkedBlockingQueue<LocalTask> queue = new LinkedBlockingQueue<LocalTask>();
    private final Project project;
    private final PluginTaskManager pluginTaskManager;
    private LocalTask prevTask = null;
    private Timer taskManager;



    public ActivationProcessingManager(Project project, PluginTaskManager pluginTaskManager) {
        this.project = project;
        this.pluginTaskManager = pluginTaskManager;
    }

    public void addTaskToQueue(final LocalTask task) {
        queue.add(task);
        taskManager.schedule(new ProcessingTask(), 0);
    }

    public void projectOpened() {
        taskManager = new Timer();                
    }

    public void projectClosed() {

    }

    @NotNull
    public String getComponentName() {
        return ActivationProcessingManager.class.getName();
    }

    public void initComponent() {


    }

    public void disposeComponent() {

    }


    class ProcessingTask extends TimerTask {
        private boolean running = false;

        @Override
        public void run() {
            while (!queue.isEmpty()) {
                LocalTask newTask = queue.poll();
                if (prevTask != newTask) {
                    if (!PluginTaskManager.isDefaultTask(newTask)) {
                        final ActiveJiraIssue jiraIssue = ActiveIssueUtils.getActiveJiraIssue(project);
                        if (jiraIssue == null || !newTask.getId().equals(jiraIssue.getIssueKey())) {

                            final JiraServerData sd = pluginTaskManager.findJiraPluginJiraServer(newTask.getIssueUrl());
                            if (sd != null) {
                                final ActiveJiraIssue ai = new ActiveJiraIssueBean(sd.getServerId(), newTask.getIssueUrl(), newTask.getId(),
                                        new DateTime());

                                ActiveIssueUtils.activateIssue(project, null, ai, sd, null);
                            }
                        }

                    } else {
                        SwingUtilities.invokeLater(new DeactivateIssueRunnable(project));
                    }
                }

                prevTask = newTask;
            }
        }
    }


}
