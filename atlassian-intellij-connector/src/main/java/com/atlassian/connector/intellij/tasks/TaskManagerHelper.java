/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.connector.intellij.tasks;

import com.intellij.openapi.project.Project;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class TaskManagerHelper {
	private static TaskManagerHelper instance;
	private static Object taskManager = null;
	private static Project project;
	private static Class taskManagerClass;
	private static Class localTaskClass;
	private static Class jiraRepositoryClass;
	private static Class jiraRepositoryTypeClass;
	private static Class taskClass;
	private static Class baseRepositoryClass;
	private static Class repositoryTypeClass;
	private static Class taskListenerClass;
	private static Class arrayListClass;

	private TaskManagerHelper(Project project) {

		TaskManagerHelper.project = project;
		try {
			taskManagerClass = Class.forName("com.intellij.tasks.TaskManager");
			Method getInstanceMethod = taskManagerClass.getMethod("getInstance", Project.class);
			taskManager = getInstanceMethod.invoke(null, project);
			localTaskClass = Class.forName("com.intellij.tasks.LocalTask");
			jiraRepositoryClass = Class.forName("com.intellij.tasks.jira.JiraRepository");
			jiraRepositoryTypeClass = Class.forName("com.intellij.tasks.jira.JiraRepositoryType");
			taskClass = Class.forName("com.intellij.tasks.Task");
			baseRepositoryClass = Class.forName("com.intellij.tasks.impl.BaseRepository");
			repositoryTypeClass = Class.forName("com.intellij.tasks.jira.JiraRepositoryType");
			taskListenerClass = Class.forName("com.intellij.tasks.TaskListener");
			arrayListClass = Class.forName("java.util.ArrayList<com.intellij.tasks.TaskRepository>");

		} catch (Exception e) {

		}

	}

	public static TaskManagerHelper getInstance(Project project) {
		if (instance == null) {
			instance = new TaskManagerHelper(project);
		}
		return instance;
	}

	public Object getTaskManagerImplInstance(Project project) {
		return taskManager;
	}

	public Object getActiveTask() {
		try {
			Method getActiveTaskMethod = taskManagerClass.getMethod("getActiveTask");
			return getActiveTaskMethod.invoke(taskManager);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[] getLocalTasks() {
		try {
			Method getLocalTasksMethod = taskManagerClass.getMethod("getLocalTasks");
			return (Object[]) getLocalTasksMethod.invoke(taskManager);
		} catch (Exception e) {
			return new Object[0];
		}
	}

	public String getLocalTaskIssueUrl(Object localTask) {
		try {
			Method getIssueUrlMethod = localTaskClass.getMethod("getIssueUrl");
			return (String) getIssueUrlMethod.invoke(localTask);
		} catch (Exception e) {
			return "";
		}
	}

	public String getLocalTaskId(Object localTask) {
		try {
			Method getIdMethod = localTaskClass.getMethod("getId");
			return (String) getIdMethod.invoke(localTask);
		} catch (Exception e) {
			return "";
		}
	}

	public String getLocalTaskSummary(Object localTask) {
		try {
			Method getSummaryMethod = localTaskClass.getMethod("getSummary");
			return (String) getSummaryMethod.invoke(localTask);
		} catch (Exception e) {
			return "";
		}
	}

	public Object[] getAllRepositories() {
		try {
			Method getAllRepositories = taskManagerClass.getMethod("getAllRepositories");
			return (Object[]) getAllRepositories.invoke(taskManager);
		} catch (Exception e) {
			return new Object[0];
		}
	}


	public void activateTask(Object foundTask, boolean b, boolean b1) {
		try {

			Method activateTaskMethod = taskManagerClass
					.getMethod("activateTask", taskClass, boolean.class, boolean.class);
			activateTaskMethod.invoke(taskManager, foundTask, b, b1);
		} catch (Exception e) {
		}
	}

	public Object jiraRepositoryfindTask(Object jiraRepository, String issueKey) {
		try {
			Method findTaskMethod = jiraRepositoryClass.getMethod("findTask", String.class);
			return findTaskMethod.invoke(jiraRepository, issueKey);

		} catch (Exception e) {
			return null;
		}

	}

	public String getBaseRepositoryTypeName(Object baseRepository) {
		try {
			Method getRepositoryTypeMethod = baseRepositoryClass.getMethod("getRepositoryType");
			Object repositoryTypeObject = getRepositoryTypeMethod.invoke(baseRepository);
			Method getNameMethod = repositoryTypeClass.getMethod("getName");
			return (String) getNameMethod.invoke(repositoryTypeObject);
		} catch (Exception e) {
			return "";
		}
	}

	public String getBaseRepositoryUrl(Object baseRepository) {
		try {
			Method getUrlMethod = baseRepositoryClass.getMethod("getUrl");
			return (String) getUrlMethod.invoke(baseRepository);
		} catch (Exception e) {
			return "";
		}
	}

	public Object createJiraRepository(String url, String username, String password) {
		try {
			Constructor jiraRepositoryTypeConstructor = jiraRepositoryTypeClass.getConstructor();
			Object jiraRepositoryTypeObject = jiraRepositoryTypeConstructor.newInstance();
			Method createRepositoryMethod = jiraRepositoryClass.getMethod("createRepository");
			Object jiraRepositoryObject = createRepositoryMethod.invoke(jiraRepositoryTypeObject);

			Method setUrlMethod = jiraRepositoryClass.getMethod("setUrl", String.class);
			setUrlMethod.invoke(jiraRepositoryObject, url);

			Method setUsernameMethod = jiraRepositoryClass.getMethod("setUsername", String.class);
			setUsernameMethod.invoke(jiraRepositoryObject, username);

			Method setPasswordMethod = jiraRepositoryClass.getMethod("setPassword", String.class);
			setPasswordMethod.invoke(jiraRepositoryObject, password);

			return jiraRepositoryObject;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public void addTaskListener(TaskListenerImpl listener) {
		try {
			Method addTaskListenerMethod = taskManagerClass.getMethod("addTaskListener", taskListenerClass);
			addTaskListenerMethod.invoke(taskManager, listener);
		} catch (Exception e) {

		}

	}

	public void removeTaskListener(TaskListenerImpl listener) {
		try {
			Method removeTaskListenerMethod = taskManagerClass.getMethod("removeTaskListener", taskListenerClass);
			removeTaskListenerMethod.invoke(taskManager, listener);
		} catch (Exception e) {

		}

	}

	public List<Object> getNewArrayList() {
		try {
			return (List<Object>) arrayListClass.newInstance();
		} catch (Exception e) {
			return null;
		}

	}

	public void setRepositories(List<Object> reposList) {
		try {
			Method setRepositoriesMethod = taskManagerClass.getMethod("setRepositories", arrayListClass);
			setRepositoriesMethod.invoke(taskManager, reposList);
		} catch (Exception e) {

		}
	}
}
