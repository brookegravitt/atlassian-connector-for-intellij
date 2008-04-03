package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.remoteapi.ProductServerFacade;

import java.util.List;

public interface JIRAServerFacade extends ProductServerFacade {
    List getIssues(Server server, List<JIRAQueryFragment> query) throws JIRAException;

	List getSavedFilterIssues(Server server, List<JIRAQueryFragment> query) throws JIRAException;	

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
}
