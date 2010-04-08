package com.atlassian.connector.intellij.tasks;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.TaskManagerImpl;
import com.intellij.tasks.jira.JiraRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @author pmaruszak
 * @date Feb 2, 2010
 */
public class PluginTaskManager implements ProjectComponent {

    private final Project project;
    private final ProjectCfgManager projectCfgManager;
    private final PluginConfiguration pluginConfiguration;
    private TaskManagerImpl taskManager;
    private TaskListenerImpl listener;
    private Timer timer = new Timer("plugin task manager timer");
    private static final int SILENT_ACTIVATE_DELAY = 500;
//    private PluginChangeListAdapter changeListListener;


    public PluginTaskManager(Project project, ProjectCfgManager projectCfgManager, PluginConfiguration pluginConfiguration) {
        this.project = project;
        this.projectCfgManager = projectCfgManager;
        this.pluginConfiguration = pluginConfiguration;
        this.listener = new TaskListenerImpl(project, this, pluginConfiguration);
        this.taskManager = (TaskManagerImpl) TaskManager.getManager(project);
//        this.changeListListener = new PluginChangeListAdapter(project);
    }

    public void silentActivateIssue(final ActiveJiraIssue issue) {
        deactivateListner();
        try {
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            activateIssue(issue);
                        }
                    });

                }
            }, SILENT_ACTIVATE_DELAY);            

        } finally {
//            taskManager.addTaskListener(listener);
        }
    }


    public void silentDeactivateIssue() {
        deactivateListner();
        try {
            deactivateToDefaultTask();
        } finally {
//            taskManager.addTaskListener(listener);
        }
    }

    public void activateIssue(ActiveJiraIssue issue) {

        Task foundTask;
        ServerData server = projectCfgManager.getServerr(issue.getServerId());
        foundTask = findLocalTaskByUrl(issue.getIssueUrl());

        //ADD or GET JiraRepository
        BaseRepository jiraRepository = getJiraRepository(server);
        if (foundTask != null) {
            LocalTask activeTask = taskManager.getActiveTask();
            if ((activeTask.getIssueUrl() != null
                    && !activeTask.getIssueUrl().equals(foundTask.getIssueUrl()))) {
                try {
                    taskManager.activateTask(foundTask, true, false);
                    activateListener();
                } catch (Exception e) {
                    PluginUtil.getLogger().error("Task haven't been activated : " + e.getMessage());
                    deactivateToDefaultTask();
                }

            }
        } else {
            Task newTask = (Task) TaskHelper.findJiraTask((JiraRepository) jiraRepository, issue.getIssueKey());

            if (newTask != null) {
                try {
                    taskManager.activateTask(newTask, true, true);
                    activateListener();
//                    return (LocalTask) newTask;
                } catch (Exception e) {
                    PluginUtil.getLogger().error("Task haven't been activated : " + e.getMessage());
                    deactivateToDefaultTask();                    
                }
            }
        }

//        return getDefaultTask();
    }

    @Nullable
    private BaseRepository getJiraRepository(ServerData server) {
        TaskRepository[] repos = taskManager.getAllRepositories();
        if (repos != null) {
            for (TaskRepository r : repos) {
                if (r.getRepositoryType().getName().equalsIgnoreCase("JIRA")
                        && r.getUrl().equalsIgnoreCase(server.getUrl())) {
                    return (BaseRepository) r;
                }
            }
        }

        return createJiraRepository(server);
    }

    @Nullable
    private BaseRepository createJiraRepository(ServerData server) {
        BaseRepository repo = (BaseRepository) TaskHelper.createJiraRepository();
        repo.setPassword(server.getPassword());
        repo.setUrl(server.getUrl());
        repo.setUsername(server.getUsername());
        addJiraRepository(repo);

        return null;
    }

    private void activateListener() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                taskManager.addTaskListener(listener);
            }
        });
    }

    private void deactivateListner() {
        taskManager.removeTaskListener(listener);
    }
    private void addJiraRepository(TaskRepository repo) {
        TaskRepository[] repos = taskManager.getAllRepositories();
        List<TaskRepository> reposList = new ArrayList<TaskRepository>();
        if (repos != null) {
            for (TaskRepository r : repos) {
                reposList.add(r);
            }
        }
        reposList.add(repo);
        taskManager.setRepositories(reposList);
    }

    @Nullable
    private LocalTask findLocalTaskByUrl
            (String
                    issueUrl) {
        LocalTask[] tasks = taskManager.getLocalTasks();
        if (tasks != null) {
            for (LocalTask t : tasks) {
                if (t.getIssueUrl() != null && t.getIssueUrl().equals(issueUrl)) {
                    return t;
                }
            }
        }

        return null;
    }

    @Nullable
    private LocalTask findLocalTaskById
            (String
                    issueId) {
        LocalTask[] tasks = taskManager.getLocalTasks();
        if (tasks != null) {
            for (LocalTask t : tasks) {
                if (t.getId() != null && t.getId().equals(issueId)) {
                    return t;
                }
            }
        }

        return null;
    }

    public void projectOpened() {
        StartupManager.getInstance(project).registerPostStartupActivity(new Runnable() {
            public void run() {
                initializePlugin();
            }
        });
    }


    private void initializePlugin() {
        this.taskManager = (TaskManagerImpl) TaskManager.getManager(project);
        activateListener();
//        ChangeListManagerImpl.getInstance(project).addChangeListListener(changeListListener);
    }

    public void projectClosed() {
        deactivateListner();
    }

    @NotNull
    public String getComponentName() {
        return "PluginTaskManager";
    }

    public void initComponent() {

    }

    public void disposeComponent() {

    }

    public static boolean isDefaultTask(LocalTask task) {
        return (task.getId() != null && task.getId().equalsIgnoreCase("Default")) || task.getSummary().equalsIgnoreCase("Default task");
    }

    @Nullable
    JiraServerData findJiraPluginJiraServer(String issueUrl) {
        for (JiraServerData server : projectCfgManager.getAllEnabledJiraServerss()) {
            if (issueUrl != null && issueUrl.contains(server.getUrl())) {
                return server;
            }
        }

        return null;
    }


    public void deactivateToDefaultTask() {
        LocalTask defaultTask = getDefaultTask();
        if (defaultTask != null) {
            taskManager.activateTask(defaultTask, false, false);
        }
        activateListener();
    }

    private LocalTask getDefaultTask() {
        LocalTask defaultTask = findLocalTaskById("Default task");
        if (defaultTask == null) {
            defaultTask = findLocalTaskById("Default");
        }

        return defaultTask;
    }

}
