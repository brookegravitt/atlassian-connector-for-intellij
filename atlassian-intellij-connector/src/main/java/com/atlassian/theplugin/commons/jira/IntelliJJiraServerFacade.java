package com.atlassian.theplugin.commons.jira;

import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallback;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.api.commons.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.commons.JIRAIssueBean;
import com.atlassian.theplugin.commons.jira.api.commons.JIRAServerFacade2;
import com.atlassian.theplugin.commons.jira.api.commons.JIRAServerFacade2Impl;
import com.atlassian.theplugin.commons.jira.api.commons.JiraUserNotFoundException;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAAttachment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComponentBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAProject;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAResolutionBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAUserBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAVersionBean;
import com.atlassian.theplugin.commons.jira.api.commons.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * @author pmaruszak
 * @date Sep 28, 2009
 */
public class IntelliJJiraServerFacade implements JiraServerFacade {
    private final JIRAServerFacade2 facade;
	private static IntelliJJiraServerFacade instance;



    private IntelliJJiraServerFacade() {
		this(new JIRAServerFacade2Impl(new IntelliJHttpSessionCallback()));
	}

    public static synchronized IntelliJJiraServerFacade getInstance() {
		if (instance == null) {
			instance = new IntelliJJiraServerFacade();
		}
		return instance;
	}

    public JIRAServerFacade2 getFacade() {
        return facade;
    }

    public IntelliJJiraServerFacade(JIRAServerFacade2Impl facade) {
        this.facade = facade;
    }

    public List<JiraIssueAdapter> getIssues(final JiraServerData jiraServerData, List<JIRAQueryFragment> query,
                                           String sort, String sortOrder, int start, int size) throws JIRAException {
        List<JIRAIssue> list =
                facade.getIssues(jiraServerData.toHttpConnectionCfg(), query, sort, sortOrder, start, size);
        return getJiraServerAdapterList(jiraServerData, list);
    }


    public List<JiraIssueAdapter> getSavedFilterIssues(final JiraServerData jiraServerData,
                                                       List<JIRAQueryFragment> query, String sort, String sortOrder,
                                                       int start, int size) throws JIRAException {
        List<JIRAIssue> list =
                facade.getSavedFilterIssues(jiraServerData.toHttpConnectionCfg(), query, sort, sortOrder, start, size);
        return getJiraServerAdapterList(jiraServerData, list);
    }

    public List<JIRAProject> getProjects(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getProjects(jiraServerData.toHttpConnectionCfg());
    }

    public List<JIRAConstant> getStatuses(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getStatuses(jiraServerData.toHttpConnectionCfg());
    }

    public List<JIRAConstant> getIssueTypes(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getIssueTypes(jiraServerData.toHttpConnectionCfg());
    }

    public List<JIRAConstant> getIssueTypesForProject(final JiraServerData jiraServerData, String project)
            throws JIRAException {
        return facade.getIssueTypesForProject(jiraServerData.toHttpConnectionCfg(), project);
    }

    public List<JIRAConstant> getSubtaskIssueTypes(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getSubtaskIssueTypes(jiraServerData.toHttpConnectionCfg());
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(final JiraServerData jiraServerData, String project)
            throws JIRAException {
        return facade.getSubtaskIssueTypesForProject(jiraServerData.toHttpConnectionCfg(), project);
    }

    public List<JIRAQueryFragment> getSavedFilters(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getSavedFilters(jiraServerData.toHttpConnectionCfg());
    }

    public List<JIRAComponentBean> getComponents(final JiraServerData jiraServerData, String projectKey) throws JIRAException {
        return facade.getComponents(jiraServerData.toHttpConnectionCfg(), projectKey);
    }

    public List<JIRAVersionBean> getVersions(final JiraServerData jiraServerData, String projectKey) throws JIRAException {
        return facade.getVersions(jiraServerData.toHttpConnectionCfg(), projectKey);
    }

    public List<JIRAPriorityBean> getPriorities(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getPriorities(jiraServerData.toHttpConnectionCfg());
    }

    public List<JIRAResolutionBean> getResolutions(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getResolutions(jiraServerData.toHttpConnectionCfg());
    }

    public List<JIRAAction> getAvailableActions(final JiraServerData jiraServerData, JIRAIssue issue)
            throws JIRAException {
        return facade.getAvailableActions(jiraServerData.toHttpConnectionCfg(), issue);
    }

    public List<JIRAActionField> getFieldsForAction(final JiraServerData jiraServerData, JIRAIssue issue,
                                                    JIRAAction action) throws JIRAException {
        return facade.getFieldsForAction(jiraServerData.toHttpConnectionCfg(), issue, action);
    }

    public void progressWorkflowAction(final JiraServerData jiraServerData, JIRAIssue issue, JIRAAction action)
            throws JIRAException {
        facade.progressWorkflowAction(jiraServerData.toHttpConnectionCfg(), issue, action);
    }

    public void progressWorkflowAction(final JiraServerData jiraServerData, JIRAIssue issue, JIRAAction action,
                                       List<JIRAActionField> fields) throws JIRAException {
        facade.progressWorkflowAction(jiraServerData.toHttpConnectionCfg(), issue, action, fields);
    }

    public void addComment(final JiraServerData jiraServerData, String issueKey, String comment) throws JIRAException {
        facade.addComment(jiraServerData.toHttpConnectionCfg(), issueKey, comment);
    }


    public JiraIssueAdapter createIssue(final JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
        JIRAIssue newIssue =  facade.createIssue(jiraServerData.toHttpConnectionCfg(), issue);
        return new JiraIssueAdapter((JIRAIssueBean) newIssue, jiraServerData);
    }

    public JiraIssueAdapter getIssue(final JiraServerData jiraServerData, String key) throws JIRAException {
        JIRAIssue issue =  facade.getIssue(jiraServerData.toHttpConnectionCfg(), key);
        return new JiraIssueAdapter((JIRAIssueBean) issue, jiraServerData);
    }

    public JiraIssueAdapter getIssueDetails(final JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
         JIRAIssue i = facade.getIssueDetails(jiraServerData.toHttpConnectionCfg(), issue);
        return new JiraIssueAdapter((JIRAIssueBean) i, jiraServerData);
    }

    public void logWork(final JiraServerData jiraServerData, JIRAIssue issue, String timeSpent, Calendar startDate,
                        String comment, boolean updateEstimate, String newEstimate) throws JIRAException {
        facade.logWork(jiraServerData.toHttpConnectionCfg(), issue, timeSpent, startDate, comment, updateEstimate,
                newEstimate);
    }

    public void setAssignee(final JiraServerData jiraServerData, JIRAIssue issue, String assignee) throws JIRAException {
        facade.setAssignee(jiraServerData.toHttpConnectionCfg(), issue, assignee);
    }

    public JIRAUserBean getUser(final JiraServerData jiraServerData, String loginName)
            throws JIRAException, JiraUserNotFoundException {
        return facade.getUser(jiraServerData.toHttpConnectionCfg(), loginName);
    }

    public List<JIRAComment> getComments(final JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
        return facade.getComments(jiraServerData.toHttpConnectionCfg(), issue);
    }

    public Collection<JIRAAttachment> getIssueAttachements(final JiraServerData jiraServerData, JIRAIssue issue)
            throws JIRAException {
        return facade.getIssueAttachements(jiraServerData.toHttpConnectionCfg(), issue);
    }

    public void testServerConnection(JiraServerData jiraServerData) throws RemoteApiException {
        facade.testServerConnection(jiraServerData.toHttpConnectionCfg());
    }

    public void testServerConnection(final HttpConnectionCfg httpConnectionCfg) throws RemoteApiException {
        facade.testServerConnection(httpConnectionCfg);
    }

    public ServerType getServerType() {
        return facade.getServerType();
    }

    private List<JiraIssueAdapter> getJiraServerAdapterList(JiraServerData jiraServerData, List<JIRAIssue> list) {
        List<JiraIssueAdapter> adapterList = new ArrayList<JiraIssueAdapter>(list.size());
        for (JIRAIssue issue : list) {
            adapterList.add(new JiraIssueAdapter((JIRAIssueBean) issue, jiraServerData));
        }
        return adapterList;
    }
}
