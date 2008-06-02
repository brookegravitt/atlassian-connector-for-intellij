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

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.jira.api.*;

import java.util.Calendar;
import java.util.List;

public interface JIRAServerFacade extends ProductServerFacade {
    List getIssues(Server server, List<JIRAQueryFragment> query,
				   String sort,
				   String sortOrder,
				   int start,
				   int size) throws JIRAException;

	List getSavedFilterIssues(Server server,
							  List<JIRAQueryFragment> query,
							  String sort,
							  String sortOrder,
							  int start,
							  int size) throws JIRAException;

	List<JIRAProject> getProjects(Server server) throws JIRAException;

    List<JIRAConstant> getIssueTypes(Server server) throws JIRAException;
    
    List<JIRAConstant> getStatuses(Server server) throws JIRAException;

	List<JIRAConstant> getIssueTypesForProject(Server server, String project) throws JIRAException;

	List<JIRAQueryFragment> getSavedFilters(Server server) throws JIRAException;	

	List<JIRAComponentBean> getComponents(Server server, String projectKey) throws JIRAException;

	List<JIRAVersionBean> getVersions(Server server, String projectKey) throws JIRAException;

	List<JIRAConstant> getPriorities(Server server) throws JIRAException;

	List<JIRAResolutionBean> getResolutions(Server server) throws JIRAException;

    List<JIRAAction> getAvailableActions(Server server, JIRAIssue issue) throws JIRAException;

	List<JIRAActionField> getFieldsForAction(Server server, JIRAIssue issue, JIRAAction action) throws JIRAException;

	void progressWorkflowAction(Server server, JIRAIssue issue, JIRAAction action) throws JIRAException;

	void addComment(Server server, JIRAIssue issue, String comment) throws JIRAException;

    JIRAIssue createIssue(Server server, JIRAIssue issue) throws JIRAException;

	void logWork(Server server, JIRAIssue issue, String timeSpent, Calendar startDate, String comment)
			throws JIRAException;

	void setAssignee(Server server, JIRAIssue issue, String assignee) throws JIRAException;
}
