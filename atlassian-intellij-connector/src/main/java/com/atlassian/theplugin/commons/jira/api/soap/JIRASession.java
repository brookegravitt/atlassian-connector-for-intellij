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

package com.atlassian.theplugin.commons.jira.api.soap;

import com.atlassian.theplugin.commons.jira.api.*;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.Calendar;
import java.util.List;
import java.util.Collection;

public interface JIRASession {
	void login(String userName, String password) throws RemoteApiException;

	void logout();

	void logWork(JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
			boolean updateEstimate, String newEstimate)
			throws RemoteApiException;

	void addComment(String issueKey, String comment) throws RemoteApiException;

	JIRAIssue createIssue(JIRAIssue issue) throws RemoteApiException;

	JIRAIssue getIssueDetails(JIRAIssue issue) throws RemoteApiException;

	List<JIRAProject> getProjects() throws RemoteApiException;

	List<JIRAConstant> getIssueTypes() throws RemoteApiException;

	List<JIRAConstant> getIssueTypesForProject(String project) throws RemoteApiException;

	List<JIRAConstant> getSubtaskIssueTypes() throws RemoteApiException;

	List<JIRAConstant> getSubtaskIssueTypesForProject(String project) throws RemoteApiException;

	List<JIRAConstant> getStatuses() throws RemoteApiException;

	List<JIRAComponentBean> getComponents(String projectKey) throws RemoteApiException;

	List<JIRAVersionBean> getVersions(String projectKey) throws RemoteApiException;

	List<JIRAPriorityBean> getPriorities() throws RemoteApiException;

	List<JIRAResolutionBean> getResolutions() throws RemoteApiException;

	List<JIRAQueryFragment> getSavedFilters() throws RemoteApiException;

	List<JIRAAction> getAvailableActions(JIRAIssue issue) throws RemoteApiException;

	List<JIRAActionField> getFieldsForAction(JIRAIssue issue, JIRAAction action) throws RemoteApiException;

	void progressWorkflowAction(JIRAIssue issue, JIRAAction action, List<JIRAActionField> fields) throws RemoteApiException;

	void setAssignee(JIRAIssue issue, String assignee) throws RemoteApiException;

	JIRAUserBean getUser(String loginName) throws RemoteApiException, JiraUserNotFoundException;

	List<JIRAComment> getComments(JIRAIssue issue) throws RemoteApiException;

	boolean isLoggedIn();

    Collection<JIRAAttachment> getIssueAttachements(JIRAIssue issue) throws RemoteApiException;
}
