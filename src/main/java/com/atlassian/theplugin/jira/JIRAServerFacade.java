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

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.remoteapi.ProductServerFacade;

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

	List getIssueTypesForProject(Server server, String project) throws JIRAException;

	List getSavedFilters(Server server) throws JIRAException;	

	List getComponents(Server server, String projectKey) throws JIRAException;

	List getVersions(Server server, String projectKey) throws JIRAException;

	List getPriorieties(Server server) throws JIRAException;

	List getResolutions(Server server) throws JIRAException;
	
	void addComment(Server server, JIRAIssue issue, String comment) throws JIRAException;

    JIRAIssue createIssue(Server server, JIRAIssue issue) throws JIRAException;

	void logWork(Server server, JIRAIssue issue, String timeSpent, String comment) throws JIRAException;
}
