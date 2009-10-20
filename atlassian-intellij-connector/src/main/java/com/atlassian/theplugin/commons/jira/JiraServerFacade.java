package com.atlassian.theplugin.commons.jira;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.api.commons.JIRAIssue;
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
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * @author pmaruszak
 * @date Sep 28, 2009
 */
public interface JiraServerFacade extends ProductServerFacade {
    List<JiraIssueAdapter> getIssues(JiraServerData jiraServerData, List<JIRAQueryFragment> query,
                                     String sort, String sortOrder, int start, int size) throws JIRAException;

    List<JiraIssueAdapter> getSavedFilterIssues(JiraServerData jiraServerData, List<JIRAQueryFragment> query,
                                                String sort, String sortOrder, int start, int size) throws JIRAException;

    List<JIRAProject> getProjects(JiraServerData jiraServerData) throws JIRAException;

    List<JIRAConstant> getStatuses(JiraServerData jiraServerData) throws JIRAException;

    List<JIRAConstant> getIssueTypes(JiraServerData jiraServerData) throws JIRAException;

    List<JIRAConstant> getIssueTypesForProject(JiraServerData jiraServerData, String project) throws JIRAException;

    List<JIRAConstant> getSubtaskIssueTypes(JiraServerData jiraServerData) throws JIRAException;

    List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerData jiraServerData, String project) throws JIRAException;

    List<JIRAQueryFragment> getSavedFilters(JiraServerData jiraServerData) throws JIRAException;

    List<JIRAComponentBean> getComponents(JiraServerData jiraServerData, String projectKey) throws JIRAException;

    List<JIRAVersionBean> getVersions(JiraServerData jiraServerData, String projectKey) throws JIRAException;

    List<JIRAPriorityBean> getPriorities(JiraServerData jiraServerData) throws JIRAException;

    List<JIRAResolutionBean> getResolutions(JiraServerData jiraServerData) throws JIRAException;

    List<JIRAAction> getAvailableActions(JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException;

    List<JIRAActionField> getFieldsForAction(JiraServerData jiraServerData, JIRAIssue issue, JIRAAction action)
            throws JIRAException;

    void progressWorkflowAction(JiraServerData jiraServerData, JIRAIssue issue, JIRAAction action) throws JIRAException;

    void progressWorkflowAction(JiraServerData jiraServerData, JIRAIssue issue, JIRAAction action,
                                List<JIRAActionField> fields) throws JIRAException;

    void addComment(JiraServerData jiraServerData, String issueKey, String comment) throws JIRAException;

    JiraIssueAdapter createIssue(JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException;

    JiraIssueAdapter getIssue(JiraServerData jiraServerData, String key) throws JIRAException;

    JiraIssueAdapter getIssueDetails(JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException;

    void logWork(JiraServerData jiraServerData, JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
                 boolean updateEstimate, String newEstimate) throws JIRAException;

    void setAssignee(JiraServerData jiraServerData, JIRAIssue issue, String assignee) throws JIRAException;

    JIRAUserBean getUser(JiraServerData jiraServerData, String loginName)
            throws JIRAException, JiraUserNotFoundException;

    List<JIRAComment> getComments(JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException;

    Collection<JIRAAttachment> getIssueAttachements(JiraServerData jiraServerData, JIRAIssue issue)
            throws JIRAException;

    void testServerConnection(JiraServerData jiraServerData) throws RemoteApiException;

    ServerType getServerType();
}
