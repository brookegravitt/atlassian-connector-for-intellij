package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.*;

/**
 * User: jgorycki
 * Date: Nov 19, 2008
 * Time: 3:47:30 PM
 */
public interface JIRAServerModelAsyncExecutor {
	void checkServer(JIRAServerModelAsyncExecutorListener<Boolean> listener);

	String getErrorMessage();

	void getProjects(JIRAServerModelAsyncExecutorListener<JIRAProject> listener);

	void getStatuses(JIRAServerModelAsyncExecutorListener<JIRAConstant> listener);

	void getIssueTypes(JIRAProject project, JIRAServerModelAsyncExecutorListener<JIRAConstant> listener);

	void getSavedFilters(JIRAServerModelAsyncExecutorListener<JIRAQueryFragment> listener);

	void getPriorities(JIRAServerModelAsyncExecutorListener<JIRAConstant> listener);

	void getResolutions(JIRAServerModelAsyncExecutorListener<JIRAResolutionBean> listener);

	void getVersions(JIRAProject project, JIRAServerModelAsyncExecutorListener<JIRAVersionBean> listener);

	void getFixForVersions(JIRAProject project, JIRAServerModelAsyncExecutorListener<JIRAFixForVersionBean> listener);

	void getComponents(JIRAProject project, JIRAServerModelAsyncExecutorListener<JIRAComponentBean> listener);
}
