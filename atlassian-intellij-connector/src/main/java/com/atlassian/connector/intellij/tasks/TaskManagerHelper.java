/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.connector.intellij.tasks;

import com.intellij.openapi.project.Project;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TaskManagerHelper {
	private static TaskManagerHelper instance;
	private static Object taskManagerImpl = null;
	private static Project project;
	private static Class taskManagerClass;
	private static Class localTaskClass;
	private static Class jiraRepositoryClass;
	private static Class jiraRepositoryTypeClass;
	private static Class taskClass;
	private static Class baseRepositoryClass;
	private static Class repositoryTypeClass;
	private static Class taskListenerClass;
	//private static Class taskRepositoryClass;
	private static Class taskManagerImplClass;

	private TaskManagerHelper(Project project) {

		TaskManagerHelper.project = project;
		try {
			taskManagerClass = Class.forName("com.intellij.tasks.TaskManager");
			taskManagerImplClass = Class.forName("com.intellij.tasks.impl.TaskManagerImpl");
			Method getInstanceMethod = taskManagerClass.getMethod("getManager", Project.class);
			taskManagerImpl = getInstanceMethod.invoke(null, project);
			localTaskClass = Class.forName("com.intellij.tasks.LocalTask");
			jiraRepositoryClass = Class.forName("com.intellij.tasks.jira.JiraRepository");
			jiraRepositoryTypeClass = Class.forName("com.intellij.tasks.jira.JiraRepositoryType");
			taskClass = Class.forName("com.intellij.tasks.Task");
			baseRepositoryClass = Class.forName("com.intellij.tasks.impl.BaseRepository");
			repositoryTypeClass = Class.forName("com.intellij.tasks.jira.JiraRepositoryType");
			taskListenerClass = Class.forName("com.intellij.tasks.TaskListener");
			//taskRepositoryClass = Class.forName("com.intellij.tasks.TaskRepository");


		} catch (Exception e) {
			  e.printStackTrace();
		}

	}

	public static TaskManagerHelper getInstance(Project project) {
		if (instance == null) {
			instance = new TaskManagerHelper(project);
		}
		return instance;
	}

	public Object getTaskManagerImplInstance(Project project) {
		return taskManagerImpl;
	}

	public Object getActiveTask() {
		try {
			Method getActiveTaskMethod = taskManagerClass.getMethod("getActiveTask");
			return getActiveTaskMethod.invoke(taskManagerImpl);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object[] getLocalTasks() {
		try {
			Method getLocalTasksMethod = taskManagerClass.getMethod("getLocalTasks");
			return (Object[]) getLocalTasksMethod.invoke(taskManagerImpl);
		} catch (Exception e) {
			e.printStackTrace();
			return new Object[0];
		}
	}

	public String getLocalTaskIssueUrl(Object localTask) {
		try {
			Method getIssueUrlMethod = localTaskClass.getMethod("getIssueUrl");
			return (String) getIssueUrlMethod.invoke(localTask);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String getLocalTaskId(Object localTask) {
		try {
			Method getIdMethod = localTaskClass.getMethod("getId");
			return (String) getIdMethod.invoke(localTask);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String getLocalTaskSummary(Object localTask) {
		try {
			Method getSummaryMethod = localTaskClass.getMethod("getSummary");
			return (String) getSummaryMethod.invoke(localTask);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public Object[] getAllRepositories() {
		try {
			Method getAllRepositories = taskManagerClass.getMethod("getAllRepositories");
			return (Object[]) getAllRepositories.invoke(taskManagerImpl);
		} catch (Exception e) {
			e.printStackTrace();
			return new Object[0];
		}
	}


	public void activateTask(Object foundTask, boolean b, boolean b1) {
		try {

			Method activateTaskMethod = taskManagerClass
					.getMethod("activateTask", taskClass, boolean.class, boolean.class);
			activateTaskMethod.invoke(taskManagerImpl, foundTask, b, b1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object jiraRepositoryfindTask(Object jiraRepository, String issueKey) {
		try {
			Method findTaskMethod = jiraRepositoryClass.getMethod("findTask", String.class);
			return findTaskMethod.invoke(jiraRepository, issueKey);

		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
			return "";
		}
	}

	public String getBaseRepositoryUrl(Object baseRepository) {
		try {
			Method getUrlMethod = baseRepositoryClass.getMethod("getUrl");
			return (String) getUrlMethod.invoke(baseRepository);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public Object createJiraRepository(String url, String username, String password) {
		try {
			Constructor jiraRepositoryTypeConstructor = jiraRepositoryTypeClass.getConstructor();
			Object jiraRepositoryTypeObject = jiraRepositoryTypeConstructor.newInstance();
			Method createRepositoryTypeMethod = jiraRepositoryTypeClass.getMethod("createRepository");
			Object jiraRepositoryObject = createRepositoryTypeMethod.invoke(jiraRepositoryTypeObject);

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

	public void addTaskListener(Object listener) {
		try {
			Method addTaskListenerMethod = taskManagerClass.getMethod("addTaskListener", taskListenerClass);
			addTaskListenerMethod.invoke(taskManagerImpl, listener);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void removeTaskListener(Object listener) {
		try {
			Method removeTaskListenerMethod = taskManagerClass.getMethod("removeTaskListener", taskListenerClass);
			removeTaskListenerMethod.invoke(taskManagerImpl, listener);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<Object> getNewArrayList() {
		try {
			return new ArrayList<Object>();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public void setRepositories(List<Object> reposList) {
		try {
			Method setRepositoriesMethod = taskManagerImplClass.getMethod("setRepositories", List.class);
			setRepositoriesMethod.invoke(taskManagerImpl, reposList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
