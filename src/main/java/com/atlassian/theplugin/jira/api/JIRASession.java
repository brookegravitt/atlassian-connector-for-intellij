/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.jira.api;

import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;

import java.util.List;
import java.util.Calendar;

public interface JIRASession {
	void login(String userName, String password) throws RemoteApiLoginException;

	void logout();

	void logWork(JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
				 boolean updateEstimate, String newEstimate)
			throws RemoteApiException;

	void addComment(JIRAIssue issue, String comment) throws RemoteApiException;

	JIRAIssue createIssue(JIRAIssue issue) throws RemoteApiException;

	List<JIRAProject> getProjects() throws RemoteApiException;

	List<JIRAConstant> getIssueTypes() throws RemoteApiException;

	List<JIRAConstant> getIssueTypesForProject(String project) throws RemoteApiException;
	
	List<JIRAConstant> getStatuses() throws RemoteApiException;

	List<JIRAComponentBean> getComponents(String projectKey) throws RemoteApiException;

	List<JIRAVersionBean> getVersions(String projectKey) throws RemoteApiException;

	List<JIRAConstant> getPriorities() throws RemoteApiException;

	List<JIRAResolutionBean> getResolutions() throws RemoteApiException;

	List<JIRAQueryFragment> getSavedFilters() throws RemoteApiException;

    List<JIRAAction> getAvailableActions(JIRAIssue issue) throws RemoteApiException;

	List<JIRAActionField> getFieldsForAction(JIRAIssue issue, JIRAAction action) throws RemoteApiException;

	void progressWorkflowAction(JIRAIssue issue, JIRAAction action) throws RemoteApiException;

	void setAssignee(JIRAIssue issue, String assignee) throws RemoteApiException;

	boolean isLoggedIn();
}
