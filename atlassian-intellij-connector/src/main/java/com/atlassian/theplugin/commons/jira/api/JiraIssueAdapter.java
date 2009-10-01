package com.atlassian.theplugin.commons.jira.api;

import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.commons.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.commons.JIRAIssueBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRASecurityLevelBean;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import java.util.List;

/**
 * @author pmaruszak
 * @date Sep 28, 2009
 */
public class JiraIssueAdapter implements JIRAIssue {
    private JIRAIssueBean jiraIssue;
    private JiraServerData jiraServerData;
    private LocalConfigurationListener localConfigurationListener = new LocalConfigurationListener();


    public JiraIssueAdapter(JIRAIssueBean jiraIssue, JiraServerData jiraServerData) {
        this.jiraIssue = new JIRAIssueBean(jiraIssue);
        this.jiraServerData = jiraServerData;
    }

    public JiraIssueAdapter(final JiraServerData jiraServerData) {
        this.jiraIssue = new JIRAIssueBean();
        this.jiraServerData = jiraServerData;
    }

    public JiraIssueAdapter(JiraIssueAdapter issueAdapter) {
        this.jiraIssue = new JIRAIssueBean(issueAdapter.jiraIssue);
        this.jiraServerData = issueAdapter.jiraServerData;
    }


    public JIRAIssue getJiraIssue() {
        return jiraIssue;
    }

    public JiraServerData getJiraServerData() {
        return jiraServerData;
    }

    public String getServerUrl() {
        return jiraIssue.getServerUrl();
    }

    public String getProjectUrl() {
        return jiraIssue.getProjectUrl();
    }

    public String getIssueUrl() {
        return jiraIssue.getIssueUrl();
    }

    public Long getId() {
        return jiraIssue.getId();
    }

    public String getKey() {
        return jiraIssue.getKey();
    }

    public String getProjectKey() {
        return jiraIssue.getProjectKey();
    }

    public String getStatus() {
        return jiraIssue.getStatus();
    }

    public String getStatusTypeUrl() {
        return jiraIssue.getStatusTypeUrl();
    }

    public String getSummary() {
        return jiraIssue.getSummary();
    }

    public String getEnvironment() {
        return jiraIssue.getEnvironment();
    }

    public String getType() {
        return jiraIssue.getType();
    }

    public String getTypeIconUrl() {
        return jiraIssue.getTypeIconUrl();
    }

    public String getPriority() {
        return jiraIssue.getPriority();
    }

    public String getPriorityIconUrl() {
        return jiraIssue.getPriorityIconUrl();
    }

    public String getDescription() {
        return jiraIssue.getDescription();
    }

    public JIRAConstant getTypeConstant() {
        return jiraIssue.getTypeConstant();
    }

    public JIRAConstant getStatusConstant() {
        return jiraIssue.getStatusConstant();
    }

    public JIRAPriorityBean getPriorityConstant() {
        return jiraIssue.getPriorityConstant();
    }

    public String getAssignee() {
        return jiraIssue.getAssignee();
    }

    public String getAssigneeId() {
        return jiraIssue.getAssigneeId();
    }

    public String getReporter() {
        return jiraIssue.getReporter();
    }

    public String getReporterId() {
        return jiraIssue.getReporterId();
    }

    public String getResolution() {
        return jiraIssue.getResolution();
    }

    public String getCreated() {
        return jiraIssue.getCreated();
    }

    public String getUpdated() {
        return jiraIssue.getUpdated();
    }

    public boolean isSubTask() {
        return jiraIssue.isSubTask();
    }

    public String getParentIssueKey() {
        return jiraIssue.getParentIssueKey();
    }

    public List<String> getSubTaskKeys() {
        return jiraIssue.getSubTaskKeys();
    }

    public long getPriorityId() {
        return jiraIssue.getPriorityId();
    }

    public long getStatusId() {
        return jiraIssue.getStatusId();
    }

    public long getTypeId() {
       return jiraIssue.getTypeId();
    }

    public void setAssignee(String assignee) {
        jiraIssue.setAssignee(assignee);
    }

    public List<JIRAConstant> getAffectsVersions() {
        return jiraIssue.getAffectsVersions();
    }

    public List<JIRAConstant> getFixVersions() {
        return jiraIssue.getFixVersions();
    }

    public List<JIRAConstant> getComponents() {
        return jiraIssue.getComponents();
    }

    public void setAffectsVersions(List<JIRAConstant> versions) {
        jiraIssue.setAffectsVersions(versions);
    }

    public void setFixVersions(List<JIRAConstant> versions) {
        jiraIssue.setFixVersions(versions);
    }

    public void setComponents(List<JIRAConstant> components) {
        jiraIssue.setComponents(components);
    }

    public String getOriginalEstimate() {
        return jiraIssue.getOriginalEstimate();
    }

    public String getOriginalEstimateInSeconds() {
        return jiraIssue.getOriginalEstimateInSeconds();
    }

    public void setOriginalEstimate(String t) {
        jiraIssue.setOriginalEstimate(t);
    }

    public String getRemainingEstimate() {
        return jiraIssue.getRemainingEstimate();
    }

    public String getRemainingEstimateInSeconds() {
        return jiraIssue.getRemainingEstimateInSeconds();
    }

    public void setRemainingEstimate(String t) {
        jiraIssue.setRemainingEstimate(t);
    }

    public String getTimeSpent() {
        return jiraIssue.getTimeSpent();
    }

    public String getTimeSpentInSeconds() {
        return jiraIssue.getTimeSpentInSeconds();
    }

    public void setTimeSpent(String t) {
        jiraIssue.setTimeSpent(t);
    }

    public List<JIRAComment> getComments() {
        return jiraIssue.getComments();
    }

    public Object getRawSoapIssue() {
        return jiraIssue.getRawSoapIssue();
    }

    public void setRawSoapIssue(Object soapIssue) {
        jiraIssue.setRawSoapIssue(soapIssue);
    }

    public JIRASecurityLevelBean getSecurityLevel() {
        return jiraIssue.getSecurityLevel();
    }

    public void setJiraServerData(JiraServerData jiraServerData) {
        this.jiraServerData = jiraServerData;
    }

    public void setKey(String s) {
        jiraIssue.setKey(s);
    }

    public void setSummary(String summary) {
        jiraIssue.setSummary(summary);
    }

    public LocalConfigurationListener getLocalConfigurationListener() {
        return localConfigurationListener;
    }
    private class LocalConfigurationListener extends ConfigurationListenerAdapter {
        @Override
        public void serverDataChanged(ServerData serverData) {
            if (serverData.getServerId().equals(jiraServerData.getServerId())) {
                if (serverData instanceof JiraServerData) {
                    jiraServerData = (JiraServerData) serverData;
                } else {
                    assert false;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass())  {
            return false;
        }

        JiraIssueAdapter that = (JiraIssueAdapter) o;

        if (!jiraIssue.equals(that.jiraIssue)) {
            return false;
        }
        
        if (!jiraServerData.equals(that.jiraServerData)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = jiraIssue.hashCode();
        result = 31 * result + jiraServerData.hashCode();
        return result;
    }
}
