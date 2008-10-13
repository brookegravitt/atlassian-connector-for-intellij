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

package com.atlassian.theplugin.jira.api;

import org.jdom.Element;

import java.util.List;
import java.util.Map;

public class JIRAIssueBean implements JIRAIssue {
    private String serverUrl;
    private Long id;
    private String key;
    private String summary;
    private String status;
    private String statusUrl;
    private String type;
    private String typeUrl;
    private String priority;
    private String priorityUrl;
    private String description;
    private String projectKey;
    private JIRAConstant statusConstant;
    private JIRAConstant typeConstant;
    private JIRAConstant priorityConstant;
    private String assignee;
    private String assigneeId;
    private String reporter;
    private String reporterId;
    private String resolution;
    private String created;
    private String updated;
    private long statusId;
    private long priorityId;
    private long typeId;
	private List<JIRAConstant> affectsVersions;
	private List<JIRAConstant> fixVersions;
	private List<JIRAConstant> components;

	public JIRAIssueBean() {
    }

    public JIRAIssueBean(String serverUrl) {
        this.serverUrl = serverUrl;
    }

	public JIRAIssueBean(JIRAIssue issue) {
		serverUrl = issue.getServerUrl();
		id = issue.getId();
		key = issue.getKey();
		summary = issue.getSummary();
		status = issue.getStatus();
		statusUrl = issue.getStatusTypeUrl();
		type = issue.getType();
		typeUrl = issue.getTypeIconUrl();
		priority = issue.getPriority();
		priorityUrl = issue.getPriorityIconUrl();
		description = issue.getDescription();
		projectKey = issue.getProjectKey();
		statusConstant = issue.getStatusConstant();
		typeConstant = issue.getTypeConstant();
		priorityConstant = issue.getTypeConstant();
		assignee = issue.getAssignee();
		assigneeId = issue.getAssigneeId();
		reporter = issue.getReporter();
		reporterId = issue.getReporterId();
		resolution = issue.getResolution();
		created = issue.getCreated();
		updated = issue.getUpdated();
		statusId = issue.getStatusId();
		priorityId = issue.getPriorityId();
		typeId = issue.getTypeId();

	}

	public JIRAIssueBean(String serverUrl, Element e) {
        this.serverUrl = serverUrl;
        this.summary = getTextSafely(e, "summary");
        this.key = getTextSafely(e, "key");
        this.id = new Long(getAttributeSafely(e, "key", "id"));
        updateProjectKey();
        this.status = getTextSafely(e, "status");
        this.statusUrl = getAttributeSafely(e, "status", "iconUrl");
        try {
            this.statusId = Long.parseLong(getAttributeSafely(e, "status", "id"));
        } catch (NumberFormatException ex) {
            this.statusId = 0;
        }
        this.priority = getTextSafely(e, "priority");
        this.priorityUrl = getAttributeSafely(e, "priority", "iconUrl");
        try {
            this.priorityId = Long.parseLong(getAttributeSafely(e, "priority", "id"));
        } catch (NumberFormatException ex) {
            this.priorityId = 0;
        }
        this.description = getTextSafely(e, "description");
        this.type = getTextSafely(e, "type");
        this.typeUrl = getAttributeSafely(e, "type", "iconUrl");
        try {
            this.typeId = Long.parseLong(getAttributeSafely(e, "type", "id"));
        } catch (NumberFormatException ex) {
            this.typeId = 0;
        }
        this.assignee = getTextSafely(e, "assignee");
        this.assigneeId = getAttributeSafely(e, "assignee", "username");
        this.reporter = getTextSafely(e, "reporter");
        this.reporterId = getAttributeSafely(e, "reporter", "username");
        this.created = getTextSafely(e, "created");
        this.updated = getTextSafely(e, "updated");
        this.resolution = getTextSafely(e, "resolution");
    }

    public JIRAConstant getPriorityConstant() {
        return priorityConstant;
    }

    public void setPriority(JIRAConstant priority) {
        this.priority = priority.getName();
        this.priorityConstant = priority;
    }

    public JIRAIssueBean(String serverUrl, Map params) {
        this.serverUrl = serverUrl;
        this.summary = (String) params.get("summary");
        this.status = (String) params.get("status");
        this.key = (String) params.get("key");
        this.id = new Long(params.get("key").toString());
        updateProjectKey();
        this.description = (String) params.get("description");
        this.type = (String) params.get("type");
        this.priority = (String) params.get("priority");
    }

    private void updateProjectKey() {
        if (key != null) {
            if (key.indexOf("-") >= 0) {
                projectKey = key.substring(0, key.indexOf("-"));
            } else {
                projectKey = key;
            }
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

    public Long getId() {
        return id;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusTypeUrl() {
        return statusUrl;
    }

    public String getPriority() {
        return priority;
    }

    public String getPriorityIconUrl() {
        return priorityUrl;
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

    public String getTypeIconUrl() {
        return typeUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JIRAConstant getTypeConstant() {
        return typeConstant;
    }

    public void setType(JIRAConstant type) {
        this.type = type.getName();
        this.typeConstant = type;
    }

    public JIRAConstant getStatusConstant() {
        return statusConstant;
    }

    public void setStatus(JIRAConstant status) {
        this.status = status.getName();
        this.statusConstant = status;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public long getPriorityId() {
        return priorityId;
    }

    public long getStatusId() {
        return statusId;
    }

    public long getTypeId() {
        return typeId;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
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

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

	public List<JIRAConstant> getAffectsVersions() {
		return affectsVersions;
	}

	public void setAffectsVersions(List<JIRAConstant> affectsVersions) {
		this.affectsVersions = affectsVersions;
	}

	public List<JIRAConstant> getFixVersions() {
		return fixVersions;
	}

	public void setFixVersions(List<JIRAConstant> fixVersions) {
		this.fixVersions = fixVersions;
	}

	public List<JIRAConstant> getComponents() {
		return components;
	}

	public void setComponents(List<JIRAConstant> components) {
		this.components = components;
	}
}
