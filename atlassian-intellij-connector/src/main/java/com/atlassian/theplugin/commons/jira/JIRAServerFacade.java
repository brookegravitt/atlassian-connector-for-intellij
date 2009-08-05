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

package com.atlassian.theplugin.commons.jira;

import com.atlassian.theplugin.commons.jira.api.*;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import java.util.Calendar;
import java.util.List;

public interface JIRAServerFacade extends ProductServerFacade {    
	List<JIRAIssue> getIssues(ServerData server, List<JIRAQueryFragment> query,
			String sort,
			String sortOrder,
			int start,
			int size) throws JIRAException;

	List<JIRAIssue> getSavedFilterIssues(ServerData server,
			List<JIRAQueryFragment> query,
			String sort,
			String sortOrder,
			int start,
			int size) throws JIRAException;

	List<JIRAProject> getProjects(ServerData server) throws JIRAException;

	List<JIRAConstant> getStatuses(ServerData server) throws JIRAException;

	List<JIRAConstant> getIssueTypes(ServerData server) throws JIRAException;

	List<JIRAConstant> getIssueTypesForProject(ServerData server, String project) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypes(ServerData server) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypesForProject(ServerData server, String project) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(ServerData server) throws JIRAException;

	List<JIRAComponentBean> getComponents(ServerData server, String projectKey) throws JIRAException;

	List<JIRAVersionBean> getVersions(ServerData server, String projectKey) throws JIRAException;

	List<JIRAPriorityBean> getPriorities(ServerData server) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(ServerData server) throws JIRAException;

	List<JIRAAction> getAvailableActions(ServerData server, JIRAIssue issue) throws JIRAException;

	List<JIRAActionField> getFieldsForAction(ServerData server, JIRAIssue issue, JIRAAction action) throws JIRAException;

	void progressWorkflowAction(ServerData server, JIRAIssue issue, JIRAAction action) throws JIRAException;

	void progressWorkflowAction(ServerData server, JIRAIssue issue,
			JIRAAction action, List<JIRAActionField> fields) throws JIRAException;

	void addComment(ServerData server, String issueKey, String comment) throws JIRAException;

	JIRAIssue createIssue(ServerData server, JIRAIssue issue) throws JIRAException;

	JIRAIssue getIssue(ServerData server, String key) throws JIRAException;

	JIRAIssue getIssueDetails(ServerData server, JIRAIssue issue) throws JIRAException;

	void logWork(ServerData server, JIRAIssue issue, String timeSpent, Calendar startDate,
			String comment, boolean updateEstimate, String newEstimate)
			throws JIRAException;

	void setAssignee(ServerData server, JIRAIssue issue, String assignee) throws JIRAException;

	JIRAUserBean getUser(ServerData server, String loginName) throws JIRAException, JiraUserNotFoundException;

	List<JIRAComment> getComments(ServerData server, JIRAIssue issue) throws JIRAException;
}
