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

package com.atlassian.theplugin.commons.jira.api.commons;

import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.connector.commons.jira.JIRAAction;
import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.JiraUserNotFoundException;
import com.atlassian.connector.commons.jira.beans.JIRAAttachment;
import com.atlassian.connector.commons.jira.beans.JIRAComment;
import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRAUserBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public interface JIRAServerFacade2 extends ProductServerFacade {    
	List<JIRAIssue> getIssues(HttpConnectionCfg httpConnectionCfg, List<JIRAQueryFragment> query,
			String sort,
			String sortOrder,
			int start,
			int size) throws JIRAException;

	List<JIRAIssue> getSavedFilterIssues(HttpConnectionCfg httpConnectionCfg,
			List<JIRAQueryFragment> query,
			String sort,
			String sortOrder,
			int start,
			int size) throws JIRAException;

	List<JIRAProject> getProjects(HttpConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAConstant> getStatuses(HttpConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAConstant> getIssueTypes(HttpConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAConstant> getIssueTypesForProject(HttpConnectionCfg httpConnectionCfg, String project) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypes(HttpConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAConstant> getSubtaskIssueTypesForProject(HttpConnectionCfg httpConnectionCfg, String project) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(HttpConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAComponentBean> getComponents(HttpConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException;

	List<JIRAVersionBean> getVersions(HttpConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException;

	List<JIRAPriorityBean> getPriorities(HttpConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(HttpConnectionCfg httpConnectionCfg) throws JIRAException;

	List<JIRAAction> getAvailableActions(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException;

	List<JIRAActionField> getFieldsForAction(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action)
            throws JIRAException;

	void progressWorkflowAction(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action)
            throws JIRAException;

	void progressWorkflowAction(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue,
			JIRAAction action, List<JIRAActionField> fields) throws JIRAException;

	void addComment(HttpConnectionCfg httpConnectionCfg, String issueKey, String comment) throws JIRAException;

	JIRAIssue createIssue(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException;

	JIRAIssue getIssue(HttpConnectionCfg httpConnectionCfg, String key) throws JIRAException;

	JIRAIssue getIssueDetails(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException;

	void logWork(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue, String timeSpent, Calendar startDate,
			String comment, boolean updateEstimate, String newEstimate)
			throws JIRAException;

	void setAssignee(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue, String assignee) throws JIRAException;

	JIRAUserBean getUser(HttpConnectionCfg httpConnectionCfg, String loginName)
            throws JIRAException, JiraUserNotFoundException, com.atlassian.connector.commons.jira.JiraUserNotFoundException;

	List<JIRAComment> getComments(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException;

    Collection<JIRAAttachment> getIssueAttachements(HttpConnectionCfg httpConnectionCfg, JIRAIssue issue)
            throws JIRAException;
}
