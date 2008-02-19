package com.atlassian.theplugin.jira.api;

import org.jdom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class JIRAIssueBean implements JIRAIssue {
    private String serverUrl;
    private String key;
    private String summary;
    private String type;
    private URL typeUrl;
    private String description;
    private String projectKey;
    private JIRAConstant typeConstant;
    private String assignee;

    public JIRAIssueBean() {
    }

    public JIRAIssueBean(String serverUrl, Element e) {
        this.serverUrl = serverUrl;
        this.summary = getTextSafely(e, "summary");
        this.key = getTextSafely(e, "key");
        updateProjectKey();
        this.description = getTextSafely(e, "description");
        this.type = getTextSafely(e, "type");
        String typeUrlString = getAttributeSafely(e, "type", "iconUrl");
        if (typeUrlString != null) {
            try {
                typeUrl = new URL(typeUrlString);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
        }
    }

    public JIRAIssueBean(String serverUrl, Map params) {
        this.serverUrl = serverUrl;
        this.summary = (String) params.get("summary");
        this.key = (String) params.get("key");
        updateProjectKey();
        this.description = (String) params.get("description");
        this.type = (String) params.get("type");
    }

    private void updateProjectKey() {
        if (key != null) {
            projectKey = key.substring(0, key.indexOf("-"));
        }
    }

    private String getTextSafely(Element e, String name) {
        Element child = e.getChild(name);

        if (child == null) {
            return null;
        }

        return child.getText();
    }

    private String getAttributeSafely(Element e, String elementName, String attributeName) {
        Element child = e.getChild(elementName);

        if (child == null || child.getAttribute(attributeName) == null) {
            return null;
        }

        return child.getAttributeValue(attributeName);
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getProjectUrl() {
        return serverUrl + "/browse/" + getProjectKey();
    }

    public String getIssueUrl() {
        return serverUrl + "/browse/" + getKey();
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSummary() {
        return summary;
    }

    public String getType() {
        return type;
    }

    public JIRAConstant getTypeConstant() {
        return typeConstant;
    }

    public URL getTypeIconUrl() {
        return typeUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JIRAIssueBean that = (JIRAIssueBean) o;

        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }
        if (serverUrl != null ? !serverUrl.equals(that.serverUrl) : that.serverUrl != null) {
            return false;
        }
        if (summary != null ? !summary.equals(that.summary) : that.summary != null) {
            return false;
        }

        return true;
    }

    private static final int ONE_EFF = 31;
    public int hashCode() {
        int result;
        result = (serverUrl != null ? serverUrl.hashCode() : 0);
        result = ONE_EFF * result + (key != null ? key.hashCode() : 0);
        result = ONE_EFF * result + (summary != null ? summary.hashCode() : 0);
        return result;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(JIRAConstant type) {
        this.type = type.getName();
        this.typeConstant = type;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
