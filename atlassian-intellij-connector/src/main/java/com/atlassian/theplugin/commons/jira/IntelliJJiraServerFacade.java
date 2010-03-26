package com.atlassian.theplugin.commons.jira;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.JIRAAction;
import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.JIRAIssueBean;
import com.atlassian.connector.commons.jira.JIRAServerFacade2;
import com.atlassian.connector.commons.jira.JIRAServerFacade2Impl;
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
import com.atlassian.connector.intellij.remoteapi.IntelliJAxisSessionCallback;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallback;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * @author pmaruszak
 */
public final class IntelliJJiraServerFacade implements JiraServerFacade {
    private final JIRAServerFacade2 facade;
	private static IntelliJJiraServerFacade instance;
    private static JIRAServerModel serverModel;

    private IntelliJJiraServerFacade() {
		this(new JIRAServerFacade2Impl(new IntelliJHttpSessionCallback(), new IntelliJAxisSessionCallback()));
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

    public static void setServerModel(JIRAServerModel serverModel) {
        IntelliJJiraServerFacade.serverModel = serverModel;
    }

    private IntelliJJiraServerFacade(JIRAServerFacade2Impl facade) {
        this.facade = facade;
    }

    public List<JiraIssueAdapter> getIssues(JiraServerData jiraServerData, String queryString,
                                            String sort, String sortOrder, int start, int size) throws JIRAException {
        List<JIRAIssue> list =
                facade.getIssues(jiraServerData, queryString, sort, sortOrder, start, size);
        return getJiraServerAdapterList(jiraServerData, list);
    }

    public List<JiraIssueAdapter> getIssues(final JiraServerData jiraServerData, List<JIRAQueryFragment> query,
                                           String sort, String sortOrder, int start, int size) throws JIRAException {
        List<JIRAIssue> list =
                facade.getIssues(jiraServerData, query, sort, sortOrder, start, size);
        return getJiraServerAdapterList(jiraServerData, list);
    }


    public List<JiraIssueAdapter> getSavedFilterIssues(final JiraServerData jiraServerData,
                                                       List<JIRAQueryFragment> query, String sort, String sortOrder,
                                                       int start, int size) throws JIRAException {
        List<JIRAIssue> list =
                facade.getSavedFilterIssues(jiraServerData, query, sort, sortOrder, start, size);
        return getJiraServerAdapterList(jiraServerData, list);
    }

    public List<JIRAProject> getProjects(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getProjects(jiraServerData);
    }

    public List<JIRAConstant> getStatuses(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getStatuses(jiraServerData);
    }

    public List<JIRAConstant> getIssueTypes(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getIssueTypes(jiraServerData);
    }

    public List<JIRAConstant> getIssueTypesForProject(final JiraServerData jiraServerData, String project)
            throws JIRAException {
        return facade.getIssueTypesForProject(jiraServerData, project);
    }

    public List<JIRAConstant> getSubtaskIssueTypes(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getSubtaskIssueTypes(jiraServerData);
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(final JiraServerData jiraServerData, String project)
            throws JIRAException {
        return facade.getSubtaskIssueTypesForProject(jiraServerData, project);
    }

    public List<JIRAQueryFragment> getSavedFilters(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getSavedFilters(jiraServerData);
    }

    public List<JIRAComponentBean> getComponents(final JiraServerData jiraServerData, String projectKey) throws JIRAException {
        return facade.getComponents(jiraServerData, projectKey);
    }

    public List<JIRAVersionBean> getVersions(final JiraServerData jiraServerData, String projectKey) throws JIRAException {
        return facade.getVersions(jiraServerData, projectKey);
    }

    public List<JIRAPriorityBean> getPriorities(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getPriorities(jiraServerData);
    }

    public List<JIRAResolutionBean> getResolutions(final JiraServerData jiraServerData) throws JIRAException {
        return facade.getResolutions(jiraServerData);
    }

    public List<JIRAAction> getAvailableActions(final JiraServerData jiraServerData, JIRAIssue issue)
            throws JIRAException {
        return facade.getAvailableActions(jiraServerData, issue);
    }

    public List<JIRAActionField> getFieldsForAction(final JiraServerData jiraServerData, JIRAIssue issue,
                                                    JIRAAction action) throws JIRAException {
        return facade.getFieldsForAction(jiraServerData, issue, action);
    }

    public void progressWorkflowAction(final JiraServerData jiraServerData, JIRAIssue issue, JIRAAction action)
            throws JIRAException {
        facade.progressWorkflowAction(jiraServerData, issue, action);
    }

    public void progressWorkflowAction(final JiraServerData jiraServerData, JIRAIssue issue, JIRAAction action,
                                       List<JIRAActionField> fields) throws JIRAException {
        facade.progressWorkflowAction(jiraServerData, issue, action, fields);
    }

    public void addComment(final JiraServerData jiraServerData, String issueKey, String comment) throws JIRAException {
        facade.addComment(jiraServerData, issueKey, comment);
    }

	public void addAttachment(final JiraServerData jiraServerData, final String issueKey, final String name,
			final byte[] content) throws JIRAException {
		facade.addAttachment(jiraServerData, issueKey, name, content);
	}

    public JiraIssueAdapter createIssue(final JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
        JIRAIssue newIssue =  facade.createIssue(jiraServerData, issue);
        return new JiraIssueAdapter((JIRAIssueBean) newIssue, jiraServerData);
    }

    public JiraIssueAdapter getIssue(final JiraServerData jiraServerData, String key) throws JIRAException {
        JIRAIssue issue =  facade.getIssue(jiraServerData, key);
        serverModel.addUser(jiraServerData, issue.getAssigneeId(), issue.getAssignee());
        serverModel.addUser(jiraServerData, issue.getReporterId(), issue.getReporter());
        return new JiraIssueAdapter((JIRAIssueBean) issue, jiraServerData);
    }

    public JiraIssueAdapter getIssueDetails(final JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
         JIRAIssue i = facade.getIssueDetails(jiraServerData, issue);
        serverModel.addUser(jiraServerData, issue.getAssigneeId(), issue.getAssignee());
        serverModel.addUser(jiraServerData, issue.getReporterId(), issue.getReporter());
        return new JiraIssueAdapter((JIRAIssueBean) i, jiraServerData);
    }

    public void logWork(final JiraServerData jiraServerData, JIRAIssue issue, String timeSpent, Calendar startDate,
                        String comment, boolean updateEstimate, String newEstimate) throws JIRAException {
        facade.logWork(jiraServerData, issue, timeSpent, startDate, comment, updateEstimate,
                newEstimate);
    }

    public void setAssignee(final JiraServerData jiraServerData, JIRAIssue issue, String assignee) throws JIRAException {
        facade.setField(jiraServerData, issue, "assignee", assignee);
    }

	public void setReporter(JiraServerData jiraServerData, JIRAIssue issue, String reporter) throws JIRAException {
		 facade.setField(jiraServerData, issue, "reporter", reporter);
	}

	public void setSummary(JiraServerData jiraServerData, JIRAIssue issue, String summary) throws JIRAException {
		 facade.setField(jiraServerData, issue, "summary", summary);
	}

	public void setDescription(JiraServerData jiraServerData, JIRAIssue issue, String description) throws JIRAException {
		 facade.setField(jiraServerData, issue, "description", description);
	}

	public void setType(JiraServerData jiraServerData, JIRAIssue issue, String type) throws JIRAException {
		 facade.setField(jiraServerData, issue, "issuetype", type);
	}

	public void setPriority(JiraServerData jiraServerData, JIRAIssue issue, String priority) throws JIRAException {
		 facade.setField(jiraServerData, issue, "priority", priority);
	}

	public void setAffectedVersions(JiraServerData jiraServerData, JIRAIssue issue, String[] versions) throws JIRAException {
		facade.setField(jiraServerData, issue, "versions", versions);
	}

	public void setFixVersions(JiraServerData jiraServerData, JIRAIssue issue, String[] versions) throws JIRAException {
		facade.setField(jiraServerData, issue, "fixVersions", versions);
	}

	public void setFields(JiraServerData jiraServerData, JIRAIssue issue, List<JIRAActionField> fields) throws JIRAException {
		facade.setFields(jiraServerData, issue, fields);
	}

    public JIRAUserBean getUser(final JiraServerData jiraServerData, String loginName)
            throws JIRAException, com.atlassian.connector.commons.jira.JiraUserNotFoundException {
        return facade.getUser(jiraServerData, loginName);
    }

    public List<JIRAComment> getComments(final JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
        return facade.getComments(jiraServerData, issue);
    }

    public Collection<JIRAAttachment> getIssueAttachements(final JiraServerData jiraServerData, JIRAIssue issue)
            throws JIRAException {
        return facade.getIssueAttachements(jiraServerData, issue);
    }

    public void testServerConnection(JiraServerData jiraServerData) throws RemoteApiException {
        facade.testServerConnection(jiraServerData);
    }

    public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
        facade.testServerConnection(connectionCfg);
    }

    public ServerType getServerType() {
        return facade.getServerType();
    }

    private List<JiraIssueAdapter> getJiraServerAdapterList(JiraServerData jiraServerData, List<JIRAIssue> list) {
        List<JiraIssueAdapter> adapterList = new ArrayList<JiraIssueAdapter>(list.size());
        for (JIRAIssue issue : list) {
            adapterList.add(new JiraIssueAdapter((JIRAIssueBean) issue, jiraServerData));
            serverModel.addUser(jiraServerData, issue.getAssigneeId(), issue.getAssignee());
            serverModel.addUser(jiraServerData, issue.getReporterId(), issue.getReporter());

        }
        return adapterList;
    }
}
