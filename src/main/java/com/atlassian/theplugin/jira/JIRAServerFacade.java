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

package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.jira.api.*;

import java.util.Calendar;
import java.util.List;

public interface JIRAServerFacade extends ProductServerFacade {
    List<JIRAIssue> getIssues(JiraServerCfg server, List<JIRAQueryFragment> query,
				   String sort,
				   String sortOrder,
				   int start,
				   int size) throws JIRAException;

	List<JIRAIssue> getSavedFilterIssues(JiraServerCfg server,
							  List<JIRAQueryFragment> query,
							  String sort,
							  String sortOrder,
							  int start,
							  int size) throws JIRAException;

	List<JIRAProject> getProjects(JiraServerCfg server) throws JIRAException;

    List<JIRAConstant> getIssueTypes(JiraServerCfg server) throws JIRAException;
    
    List<JIRAConstant> getStatuses(JiraServerCfg server) throws JIRAException;

	List<JIRAConstant> getIssueTypesForProject(JiraServerCfg server, String project) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(JiraServerCfg server) throws JIRAException;

	List<JIRAComponentBean> getComponents(JiraServerCfg server, String projectKey) throws JIRAException;

	List<JIRAVersionBean> getVersions(JiraServerCfg server, String projectKey) throws JIRAException;

	List<JIRAConstant> getPriorities(JiraServerCfg server) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(JiraServerCfg server) throws JIRAException;

    List<JIRAAction> getAvailableActions(JiraServerCfg server, JIRAIssue issue) throws JIRAException;

	List<JIRAActionField> getFieldsForAction(JiraServerCfg server, JIRAIssue issue, JIRAAction action) throws JIRAException;

	void progressWorkflowAction(JiraServerCfg server, JIRAIssue issue, JIRAAction action) throws JIRAException;

	void addComment(JiraServerCfg server, JIRAIssue issue, String comment) throws JIRAException;

    JIRAIssue createIssue(JiraServerCfg server, JIRAIssue issue) throws JIRAException;

	JIRAIssue getIssueDetails(JiraServerCfg server, JIRAIssue issue) throws JIRAException;

	void logWork(JiraServerCfg server, JIRAIssue issue, String timeSpent, Calendar startDate,
				 String comment, boolean updateEstimate, String newEstimate)
			throws JIRAException;

	void setAssignee(JiraServerCfg server, JIRAIssue issue, String assignee) throws JIRAException;

	JIRAUserBean getUser(JiraServerCfg server, String loginName) throws JIRAException;

	List<JIRAComment> getComments(JiraServerCfg server, JIRAIssue issue) throws JIRAException;
}
