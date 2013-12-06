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
import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;
import com.atlassian.connector.commons.jira.beans.JIRASecurityLevelBean;
import com.atlassian.connector.commons.jira.beans.JIRAUserBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.beans.JiraFilter;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.connector.intellij.remoteapi.IntelliJAxisSessionCallback;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author pmaruszak
 */
public final class IntelliJJiraServerFacade implements JiraServerFacade {
    private final JIRAServerFacade2 facade;
	private static IntelliJJiraServerFacade instance;
    private static JIRAServerModel serverModel;

    private Map<String, Exception> bustedServers = Maps.newConcurrentMap();

    private IntelliJJiraServerFacade() {
		this(new JIRAServerFacade2Impl(new IntelliJHttpSessionCallbackImpl(), new IntelliJAxisSessionCallback()));
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

    public void reset() {
        bustedServers.clear();
        facade.reset();
    }

    public static void setServerModel(JIRAServerModel serverModel) {
        IntelliJJiraServerFacade.serverModel = serverModel;
    }

    private IntelliJJiraServerFacade(JIRAServerFacade2Impl facade) {
        this.facade = facade;
    }

//    public List<JiraIssueAdapter> getIssues(JiraServerData jiraServerData, String queryString,
//                                            String sort, String sortOrder, int start, int size) throws JIRAException {
//        List<JIRAIssue> list =
//                facade.getIssues(jiraServerData, queryString, sort, sortOrder, start, size);
//        return getJiraServerAdapterList(jiraServerData, list);
//    }
//
//    public List<JiraIssueAdapter> getIssues(final JiraServerData jiraServerData, List<JIRAQueryFragment> query,
//                                           String sort, String sortOrder, int start, int size) throws JIRAException {
//        List<JIRAIssue> list =
//                facade.getIssues(jiraServerData, query, sort, sortOrder, start, size);
//        return getJiraServerAdapterList(jiraServerData, list);
//    }


//    public List<JiraIssueAdapter> getSavedFilterIssues(final JiraServerData jiraServerData,
//                                                       List<JIRAQueryFragment> query, String sort, String sortOrder,
//                                                       int start, int size) throws JIRAException {
//        List<JIRAIssue> list =
//                facade.getSavedFilterIssues(jiraServerData, query, sort, sortOrder, start, size);
//        return getJiraServerAdapterList(jiraServerData, list);
//    }


    public boolean usesRest(final JiraServerData jiraServerData) {
        try {
            return wrap(jiraServerData, new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return facade.usesRest(jiraServerData);
                }
            }, false);
        } catch (JIRAException e) {
            return false;
        }
    }

    public List<JiraIssueAdapter> getIssues(final JiraServerData jiraServerData, final JiraFilter filter, final String sort, final String sortOrder, final int start, final int size) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JiraIssueAdapter>>() {
            public List<JiraIssueAdapter> call() throws Exception {
                List<JIRAIssue> list = facade.getIssues(jiraServerData, filter, sort, sortOrder, start, size);
                return getJiraServerAdapterList(jiraServerData, list);
            }
        }, false);
    }

    public List<JiraIssueAdapter> getIssues(final JiraServerData server, final String query, final String sort, final String sortOrder, final int start, final int size) throws JIRAException {
        return wrap(server, new Callable<List<JiraIssueAdapter>>() {
            public List<JiraIssueAdapter> call() throws Exception {
                List<JIRAIssue> list = facade.getIssues(server, query, sort, sortOrder, start, size);
                return getJiraServerAdapterList(server, list);
            }
        }, false);
    }

    public List<JiraIssueAdapter> getSavedFilterIssues(final JiraServerData jiraServerData, final JIRASavedFilter filter, final String sort, final String sortOrder, final int start, final int size) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JiraIssueAdapter>>() {
            public List<JiraIssueAdapter> call() throws Exception {
                List<JIRAIssue> list = facade.getSavedFilterIssues(jiraServerData, filter, sort, sortOrder, start, size);
                return getJiraServerAdapterList(jiraServerData, list);
            }
        }, false);
    }

    public List<JIRAProject> getProjects(final JiraServerData jiraServerData) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAProject>>() {
            public List<JIRAProject> call() throws Exception {
                return facade.getProjects(jiraServerData);
            }
        }, false);
    }

    public List<JIRAProject> getProjectsForIssueCreation(final JiraServerData jiraServerData) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAProject>>() {
            public List<JIRAProject> call() throws Exception {
                return facade.getProjectsForIssueCreation(jiraServerData);
            }
        }, false);
    }

    public List<JIRAConstant> getStatuses(final JiraServerData jiraServerData) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return facade.getStatuses(jiraServerData);
            }
        }, false);
    }

    public List<JIRAConstant> getIssueTypes(final JiraServerData jiraServerData) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return facade.getIssueTypes(jiraServerData);
            }
        }, false);
    }

    public List<JIRAConstant> getIssueTypesForProject(final JiraServerData jiraServerData, final long projectId, final String projectKey)
            throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return facade.getIssueTypesForProject(jiraServerData, projectId, projectKey);
            }
        }, false);
    }

    public List<JIRAConstant> getSubtaskIssueTypes(final JiraServerData jiraServerData) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return facade.getSubtaskIssueTypes(jiraServerData);
            }
        }, false);
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(final JiraServerData jiraServerData, final long projectId, final String projectKey)
            throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return facade.getSubtaskIssueTypesForProject(jiraServerData, projectId, projectKey);
            }
        }, false);
    }

    public List<JIRAQueryFragment> getSavedFilters(final JiraServerData jiraServerData) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAQueryFragment>>() {
            public List<JIRAQueryFragment> call() throws Exception {
                return facade.getSavedFilters(jiraServerData);
            }
        }, false);
    }

    public List<JIRAComponentBean> getComponents(final JiraServerData jiraServerData, final String projectKey) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAComponentBean>>() {
            public List<JIRAComponentBean> call() throws Exception {
                return facade.getComponents(jiraServerData, projectKey);
            }
        }, false);
    }

    public List<JIRAVersionBean> getVersions(final JiraServerData jiraServerData, final String projectKey) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAVersionBean>>() {
            public List<JIRAVersionBean> call() throws Exception {
                return facade.getVersions(jiraServerData, projectKey);
            }
        }, false);
    }

    public List<JIRAPriorityBean> getPriorities(final JiraServerData jiraServerData) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAPriorityBean>>() {
            public List<JIRAPriorityBean> call() throws Exception {
                return facade.getPriorities(jiraServerData);
            }
        }, false);
    }

    public List<JIRAResolutionBean> getResolutions(final JiraServerData jiraServerData) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAResolutionBean>>() {
            public List<JIRAResolutionBean> call() throws Exception {
                return facade.getResolutions(jiraServerData);
            }
        }, false);
    }

    public List<JIRAAction> getAvailableActions(final JiraServerData jiraServerData, final JIRAIssue issue)
            throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAAction>>() {
            public List<JIRAAction> call() throws Exception {
                return facade.getAvailableActions(jiraServerData, issue);
            }
        }, false);
    }

    public List<JIRAActionField> getFieldsForAction(final JiraServerData jiraServerData, final JIRAIssue issue,
                                                    final JIRAAction action) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAActionField>>() {
            public List<JIRAActionField> call() throws Exception {
                return facade.getFieldsForAction(jiraServerData, issue, action);
            }
        }, false);
    }

    public void progressWorkflowAction(final JiraServerData jiraServerData, final JIRAIssue issue, final JIRAAction action)
            throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.progressWorkflowAction(jiraServerData, issue, action);
                return null;
            }
        }, true);
    }

    public void progressWorkflowAction(final JiraServerData jiraServerData, final JIRAIssue issue, final JIRAAction action,
                                       final List<JIRAActionField> fields) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.progressWorkflowAction(jiraServerData, issue, action, fields);
                return null;
            }
        }, true);
    }

    public void addComment(final JiraServerData jiraServerData, final String issueKey, final String comment) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.addComment(jiraServerData, issueKey, comment);
                return null;
            }
        }, true);
    }

	public void addAttachment(final JiraServerData jiraServerData, final String issueKey, final String name,
			final byte[] content) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.addAttachment(jiraServerData, issueKey, name, content);
                return null;
            }
        }, true);
	}

    public JiraIssueAdapter createIssue(final JiraServerData jiraServerData, final JIRAIssue issue) throws JIRAException {
        return wrap(jiraServerData, new Callable<JiraIssueAdapter>() {
            public JiraIssueAdapter call() throws Exception {
                JIRAIssue newIssue =  facade.createIssue(jiraServerData, issue);
                return new JiraIssueAdapter((JIRAIssueBean) newIssue, jiraServerData);
            }
        }, true);
    }

    public JiraIssueAdapter createSubtask(final JiraServerData jiraServerData, final JIRAIssue parent, final JIRAIssue issue) throws JIRAException {
        return wrap(jiraServerData, new Callable<JiraIssueAdapter>() {
            public JiraIssueAdapter call() throws Exception {
                JIRAIssue newIssue =  facade.createSubtask(jiraServerData, parent, issue);
                return new JiraIssueAdapter((JIRAIssueBean) newIssue, jiraServerData);
            }
        }, true);
    }

    public JiraIssueAdapter getIssue(final JiraServerData jiraServerData, final String key) throws JIRAException {
        Exception exception = bustedServers.get(jiraServerData.getId());
        if (exception != null) {
            throw new JIRAException(exception.getMessage(), exception);
        }
        JIRAIssue issue =  facade.getIssue(jiraServerData, key);
        serverModel.addUser(jiraServerData, issue.getAssigneeId(), issue.getAssignee());
        serverModel.addUser(jiraServerData, issue.getReporterId(), issue.getReporter());
        return new JiraIssueAdapter((JIRAIssueBean) issue, jiraServerData);
    }

    public JiraIssueAdapter getIssueDetails(final JiraServerData jiraServerData, final JIRAIssue issue) throws JIRAException {
        Exception exception = bustedServers.get(jiraServerData.getId());
        if (exception != null) {
            throw new JIRAException(exception.getMessage(), exception);
        }
        JIRAIssue i = facade.getIssueDetails(jiraServerData, issue);
        serverModel.addUser(jiraServerData, issue.getAssigneeId(), issue.getAssignee());
        serverModel.addUser(jiraServerData, issue.getReporterId(), issue.getReporter());
        return new JiraIssueAdapter((JIRAIssueBean) i, jiraServerData);
    }

    public void logWork(final JiraServerData jiraServerData, final JIRAIssue issue, final String timeSpent, final Calendar startDate,
                        final String comment, final boolean updateEstimate, final String newEstimate) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.logWork(jiraServerData, issue, timeSpent, startDate, comment, updateEstimate, newEstimate);
                return null;
            }
        }, true);
    }

    public void setAssignee(final JiraServerData jiraServerData, final JIRAIssue issue, final String assignee) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.setField(jiraServerData, issue, "assignee", assignee);
                return null;
            }
        }, true);
    }

    public void setReporter(final JiraServerData jiraServerData, final JIRAIssue issue, final String reporter) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.setField(jiraServerData, issue, "reporter", reporter);
                return null;
            }
        }, true);
	}

	public void setSummary(final JiraServerData jiraServerData, final JIRAIssue issue, final String summary) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.setField(jiraServerData, issue, "summary", summary);
                return null;
            }
        }, true);
	}

	public void setDescription(final JiraServerData jiraServerData, final JIRAIssue issue, final String description) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.setField(jiraServerData, issue, "description", description);
                return null;
            }
        }, true);
	}

	public void setType(final JiraServerData jiraServerData, final JIRAIssue issue, final String type) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.setField(jiraServerData, issue, "issuetype", type);
                return null;
            }
        }, true);
	}

	public void setPriority(final JiraServerData jiraServerData, final JIRAIssue issue, final String priority) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.setField(jiraServerData, issue, "priority", priority);
                return null;
            }
        }, true);
	}

	public void setAffectedVersions(final JiraServerData jiraServerData, final JIRAIssue issue, final String[] versions) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.setField(jiraServerData, issue, "versions", versions);
                return null;
            }
        }, true);
	}

	public void setFixVersions(final JiraServerData jiraServerData, final JIRAIssue issue, final String[] versions) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.setField(jiraServerData, issue, "fixVersions", versions);
                return null;
            }
        }, true);
	}

	public void setFields(final JiraServerData jiraServerData, final JIRAIssue issue, final List<JIRAActionField> fields) throws JIRAException {
        wrap(jiraServerData, new Callable<Object>() {
            public Object call() throws Exception {
                facade.setFields(jiraServerData, issue, fields);
                return null;
            }
        }, true);
	}

    public JIRAUserBean getUser(final JiraServerData jiraServerData, final String loginName)
            throws JIRAException, com.atlassian.connector.commons.jira.JiraUserNotFoundException {
        return wrap(jiraServerData, new Callable<JIRAUserBean>() {
            public JIRAUserBean call() throws Exception {
                return facade.getUser(jiraServerData, loginName);
            }
        }, false);

    }

    public List<JIRAComment> getComments(final JiraServerData jiraServerData, final JIRAIssue issue) throws JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRAComment>>() {
            public List<JIRAComment> call() throws Exception {
                return facade.getComments(jiraServerData, issue);
            }
        }, false);
    }

    public Collection<JIRAAttachment> getIssueAttachements(final JiraServerData jiraServerData, final JIRAIssue issue)
            throws JIRAException {
        return wrap(jiraServerData, new Callable<Collection<JIRAAttachment>>() {
            public Collection<JIRAAttachment> call() throws Exception {
                return facade.getIssueAttachements(jiraServerData, issue);
            }
        }, false);
    }

    public void testServerConnection(final JiraServerData jiraServerData) throws RemoteApiException {
        try {
            wrap(jiraServerData, new Callable<Object>() {
                public Object call() throws Exception {
                    facade.testServerConnection(jiraServerData);
                    bustedServers.remove(jiraServerData.getId());
                    return null;
                }
            }, false);
        } catch (JIRAException e) {
            throw new RemoteApiException(e);
        }
    }

    public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
        Exception exception = bustedServers.get(connectionCfg.getId());
        if (exception != null) {
            throw new RemoteApiException(exception.getMessage(), exception);
        }
        try {
            facade.testServerConnection(connectionCfg);
            bustedServers.remove(connectionCfg.getId());
        } catch (RemoteApiException e) {
            bustedServers.put(connectionCfg.getId(), e);
            throw e;
        }
    }

    public ServerType getServerType() {
        return facade.getServerType();
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(final JiraServerData jiraServerData, final String projectKey)
            throws RemoteApiException, JIRAException {
        return wrap(jiraServerData, new Callable<List<JIRASecurityLevelBean>>() {
            public List<JIRASecurityLevelBean> call() throws Exception {
                return facade.getSecurityLevels(jiraServerData, projectKey);
            }
        }, false);
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

    private <T> T wrap(JiraServerData server, Callable<T> callable, boolean doNotBust) throws JIRAException {
        Exception ex = bustedServers.get(server.getId());
        if (ex != null) {
            if (ex instanceof JIRAException) {
                throw (JIRAException) ex;
            }
            throw new JIRAException(ex.getMessage(), ex);
        }
        try {
            return callable.call();
        } catch (Exception e) {
            JIRAException exception = new JIRAException(e.getMessage(), e);
            if (!doNotBust) {
                bustedServers.put(server.getId(), exception);
            }
            throw exception;
        }
    }
}
