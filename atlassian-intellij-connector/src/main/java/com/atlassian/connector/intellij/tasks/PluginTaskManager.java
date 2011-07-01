package com.atlassian.connector.intellij.tasks;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks.PluginTaskManagerHelper;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Timer;


/**
 * @author pmaruszak
 * @date Feb 2, 2010
 */
public class PluginTaskManager {

	private final Project project;
	private final ProjectCfgManager projectCfgManager;
	private final PluginConfiguration pluginConfiguration;
	private TaskListenerImpl listener;
	private Timer timer = new Timer("plugin task manager timer");
	private static final int SILENT_ACTIVATE_DELAY = 500;
//    private PluginChangeListAdapter changeListListener;


	public PluginTaskManager(Project project, ProjectCfgManager projectCfgManager, PluginConfiguration pluginConfiguration) {
		this.project = project;
		this.projectCfgManager = projectCfgManager;
		this.pluginConfiguration = pluginConfiguration;
		listener =
				PluginTaskManagerHelper.isValidIdeaVersion() ? new TaskListenerImpl(project, this, pluginConfiguration)
						: null;
//        this.changeListListener = new PluginChangeListAdapter(project);
	}

	public void activateIssue(final ActiveJiraIssue issue) {

		ServerData server = projectCfgManager.getServerr(issue.getServerId());
		final Object foundTask = findLocalTaskByUrl(issue.getIssueUrl());
		final TaskManagerHelper instance = TaskManagerHelper.getInstance(project);

		//ADD or GET JiraRepository
		Object jiraRepository = getJiraRepository(server);
		if (foundTask != null) {

			Object activeTask = instance.getActiveTask();
			String activeTaskIssueUrl = instance.getLocalTaskIssueUrl(activeTask);
			String foundTaskIssueUrl = instance.getLocalTaskIssueUrl(foundTask);
			if (activeTaskIssueUrl == null || !foundTaskIssueUrl.equals(activeTaskIssueUrl)) {
				try {
					instance.activateTask(foundTask, true, true);
				} catch (Exception e) {
					PluginUtil.getLogger().error("Task haven't been activated : " + e.getMessage());
					deactivateToDefaultTask();
				}

			}
		} else if (jiraRepository != null) {
			try {
				Object newTask = instance.jiraRepositoryfindTask(jiraRepository, issue.getIssueKey());
				if (newTask != null) {

					instance.activateTask(newTask, true, true);
				}
			} catch (Exception e) {
				PluginUtil.getLogger().error("Task haven't been activated : " + e.getMessage());
				deactivateToDefaultTask();
			}

		}
	}

	@Nullable
	private Object getJiraRepository(ServerData server) {

		TaskManagerHelper instance = TaskManagerHelper.getInstance(project);
		Object[] repos = instance.getAllRepositories();
		if (repos != null) {
			for (Object r : repos) {
				String repositoryTypeName = instance.getBaseRepositoryTypeName(r);
				String repositoryUrl = instance.getBaseRepositoryUrl(r);
				if (repositoryTypeName.equalsIgnoreCase("JIRA")
						&& repositoryUrl.equalsIgnoreCase(server.getUrl())) {
					return r;
				}
			}
		}

		return createJiraRepository(server);
	}

	@Nullable
	private Object createJiraRepository(ServerData server) {
		TaskManagerHelper instance = TaskManagerHelper.getInstance(project);
		Object jiraRepository = instance.createJiraRepository(server.getUrl(), server.getUsername(), server.getPassword());
		addJiraRepository(jiraRepository);
		return jiraRepository;
	}

	public void activateListener() {
		if (PluginTaskManagerHelper.isValidIdeaVersion()) {
//			EventQueue.invokeLater(new Runnable() {
//				public void run() {
			PluginUtil.getLogger().debug("Activating TM listener");
			TaskManagerHelper.getInstance(project).addTaskListener(listener);
//				}
//			});
		}
	}

	public void deactivateListner() {
		if (listener != null) {
			PluginUtil.getLogger().debug("Deactivating TM listener");

			TaskManagerHelper.getInstance(project).removeTaskListener(listener);
		}
	}

	private void addJiraRepository(Object repo) {
		final TaskManagerHelper instance = TaskManagerHelper.getInstance(project);
		Object[] repos = instance.getAllRepositories();
		List<Object> reposList = instance.getNewArrayList();
		if (repos != null) {
			for (Object r : repos) {
				reposList.add(r);
			}
		}
		reposList.add(repo);
		instance.setRepositories(reposList);
	}

	@Nullable
	private Object findLocalTaskByUrl
			(String
					issueUrl) {

		Object[] tasks = TaskManagerHelper.getInstance(project).getLocalTasks();
		if (tasks != null) {
			for (Object t : tasks) {
				String localTaskIssueUrl  = TaskManagerHelper.getInstance(project).getLocalTaskIssueUrl(t);
				if (localTaskIssueUrl != null && localTaskIssueUrl.equals(issueUrl)) {
					return t;
				}
			}
		}

		return null;
	}

	@Nullable
	private Object findLocalTaskById
			(String
					issueId) {

		Object[] tasks = TaskManagerHelper.getInstance(project).getLocalTasks();
		if (tasks != null) {
			for (Object t : tasks) {
				String localTaskId = TaskManagerHelper.getInstance(project).getLocalTaskId(t);
				if (localTaskId != null && localTaskId.equals(issueId)) {
					return t;
				}
			}
		}

		return null;
	}

	public static boolean isDefaultTask(Project project, Object task) {
		String localTaskId = TaskManagerHelper.getInstance(project).getLocalTaskId(task);
		String localTaskSummary = TaskManagerHelper.getInstance(project).getLocalTaskId(task);
		return (localTaskId != null && localTaskId.equalsIgnoreCase("Default"))
				|| localTaskSummary.equalsIgnoreCase("Default task");
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

		PluginUtil.getLogger().debug("deactivating to default");
		Object defaultTask = getDefaultTask();
		if (defaultTask != null) {
			TaskManagerHelper.getInstance(project).activateTask(defaultTask, false, false);

		}

	}

	public Object getDefaultTask() {
		Object defaultTask = findLocalTaskById("Default task");
		if (defaultTask == null) {
			defaultTask = findLocalTaskById("Default");
		}

		return defaultTask;
	}

	public boolean isDefaultTaskActive() {
		Object defaultTask = getDefaultTask();
		final Object activeTask =  TaskManagerHelper.getInstance(project).getActiveTask();

		return defaultTask != null && activeTask.equals(defaultTask);

	}


}
