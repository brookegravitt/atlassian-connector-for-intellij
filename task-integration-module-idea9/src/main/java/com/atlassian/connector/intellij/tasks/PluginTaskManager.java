package com.atlassian.connector.intellij.tasks;


import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.TaskManagerImpl;
import com.intellij.tasks.jira.JiraRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pmaruszak
 * @date Feb 2, 2010
 */
public class PluginTaskManager implements ProjectComponent {

    private final Project project;
    private final ProjectCfgManager projectCfgManager;
    private final TaskManagerImpl taskManager;
    private TaskListenerImpl listener;


    public PluginTaskManager(Project project, ProjectCfgManager projectCfgManager) {
        this.project = project;
        this.projectCfgManager = projectCfgManager;
        this.taskManager = (TaskManagerImpl) TaskManager.getManager(project);
        this.listener = new TaskListenerImpl(project, this);
    }

    public void silentActivateIssue(ActiveJiraIssue issue) {
        taskManager.removeTaskListener(listener);
        activateIssue(issue);
        taskManager.addTaskListener(listener);
    }


    public void silentDeactivateIssue() {
        taskManager.removeTaskListener(listener);
        deactivateToDefaultTask();
        taskManager.addTaskListener(listener);
    }
    public void activateIssue(ActiveJiraIssue issue) {

        Task foundTask;

        ServerData server = projectCfgManager.getServerr(issue.getServerId());
        foundTask = findLocalTaskByUrl(issue.getIssueUrl());        

        //ADD or GET JiraRepository
        BaseRepository jiraRepository = getJiraRepository(server);
        if (foundTask != null) {
            Task activeTask = taskManager.getActiveTask();
            if (activeTask == null || (activeTask != foundTask)) {
                final Task fFoundTask = foundTask;
                taskManager.activateTask(fFoundTask, false, true);

            } else {
                //todo search for issue ID and modify task instead of creating one
                try {
                    final Task fFoundTask = jiraRepository != null ? jiraRepository.findTask(issue.getIssueKey()) : null;
                    if (fFoundTask != null) {
                        taskManager.activateTask(fFoundTask, true, true);
                    }
                } catch (Exception e) {                   
                }

            }
        } else {
            Task newTask = (Task)TaskHelper.findJiraTask((JiraRepository)jiraRepository, issue.getIssueKey());
            if (newTask != null) {
                taskManager.activateTask(newTask, true, true);
            }
        }
    }

    @Nullable
    private BaseRepository getJiraRepository(ServerData server) {
        TaskRepository[] repos = taskManager.getAllRepositories();
        if (repos != null) {
            for (TaskRepository r : repos) {
                if (r.getRepositoryType().getName().equalsIgnoreCase("JIRA")
                        && r.getUrl().equalsIgnoreCase(server.getUrl())) {
                    return (BaseRepository)r;
                }
            }
        }

        return createJiraRepository(server);
    }

    @Nullable
    private BaseRepository createJiraRepository(ServerData  server) {
      BaseRepository repo = (BaseRepository) TaskHelper.createJiraRepository();
      repo.setPassword(server.getPassword());
      repo.setUrl(server.getUrl());
      repo.setUsername(server.getUsername());
      addJiraRepository(repo);

        return null;
    }

    private void addJiraRepository(TaskRepository repo) {
        TaskRepository[] repos = taskManager.getAllRepositories();
        List<TaskRepository> reposList = new ArrayList<TaskRepository>();
        if (repos != null) {
            for (TaskRepository r: repos) {
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
        taskManager.addTaskListener(listener);
    }

    public void projectClosed() {
        taskManager.removeTaskListener(listener);
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
    }

    private LocalTask getDefaultTask() {
        LocalTask defaultTask = findLocalTaskById("Default task");
        if (defaultTask == null) {
            defaultTask = findLocalTaskById("Default");
        }

        return defaultTask;
    }
}
