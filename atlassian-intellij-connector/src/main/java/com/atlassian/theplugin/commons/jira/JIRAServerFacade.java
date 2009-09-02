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

import java.util.Calendar;
import java.util.List;
import java.util.Collection;

public interface JIRAServerFacade extends ProductServerFacade {    
	List<JIRAIssue> getIssues(JiraServerData server, List<JIRAQueryFragment> query,
			String sort,
			String sortOrder,
			int start,
			int size) throws JIRAException;

	List<JIRAIssue> getSavedFilterIssues(JiraServerData server,
			List<JIRAQueryFragment> query,
			String sort,
			String sortOrder,
			int start,
			int size) throws JIRAException;

	List<JIRAProject> getProjects(JiraServerData server) throws JIRAException;

	List<JIRAConstant> getStatuses(JiraServerData server) throws JIRAException;

	List<JIRAConstant> getIssueTypes(JiraServerData server) throws JIRAException;

	List<JIRAConstant> getIssueTypesForProject(JiraServerData server, String project) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypes(JiraServerData server) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerData server, String project) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(JiraServerData server) throws JIRAException;

	List<JIRAComponentBean> getComponents(JiraServerData server, String projectKey) throws JIRAException;

	List<JIRAVersionBean> getVersions(JiraServerData server, String projectKey) throws JIRAException;

	List<JIRAPriorityBean> getPriorities(JiraServerData server) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(JiraServerData server) throws JIRAException;

	List<JIRAAction> getAvailableActions(JiraServerData server, JIRAIssue issue) throws JIRAException;

	List<JIRAActionField> getFieldsForAction(JiraServerData server, JIRAIssue issue, JIRAAction action) throws JIRAException;

	void progressWorkflowAction(JiraServerData server, JIRAIssue issue, JIRAAction action) throws JIRAException;

	void progressWorkflowAction(JiraServerData server, JIRAIssue issue,
			JIRAAction action, List<JIRAActionField> fields) throws JIRAException;

	void addComment(JiraServerData server, String issueKey, String comment) throws JIRAException;

	JIRAIssue createIssue(JiraServerData server, JIRAIssue issue) throws JIRAException;

	JIRAIssue getIssue(JiraServerData server, String key) throws JIRAException;

	JIRAIssue getIssueDetails(JiraServerData server, JIRAIssue issue) throws JIRAException;

	void logWork(JiraServerData server, JIRAIssue issue, String timeSpent, Calendar startDate,
			String comment, boolean updateEstimate, String newEstimate)
			throws JIRAException;

	void setAssignee(JiraServerData server, JIRAIssue issue, String assignee) throws JIRAException;

	JIRAUserBean getUser(JiraServerData server, String loginName) throws JIRAException, JiraUserNotFoundException;

	List<JIRAComment> getComments(JiraServerData server, JIRAIssue issue) throws JIRAException;

    Collection<JIRAAttachment> getIssueAttachements(JiraServerData server, JIRAIssue issue) throws JIRAException;
}
