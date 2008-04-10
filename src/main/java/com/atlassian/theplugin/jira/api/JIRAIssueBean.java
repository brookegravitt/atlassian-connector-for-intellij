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

import java.util.Map;

public class JIRAIssueBean implements JIRAIssue {
    private String serverUrl;
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

	public JIRAIssueBean() {
    }

    public JIRAIssueBean(String serverUrl, Element e) {
        this.serverUrl = serverUrl;
        this.summary = getTextSafely(e, "summary");
        this.key = getTextSafely(e, "key");
        updateProjectKey();
		this.status = getTextSafely(e, "status");
        this.statusUrl = getAttributeSafely(e, "status", "iconUrl");
		this.priority = getTextSafely(e, "priority");
        this.priorityUrl = getAttributeSafely(e, "priority", "iconUrl");		
		this.description = getTextSafely(e, "description");
        this.type = getTextSafely(e, "type");
        this.typeUrl = getAttributeSafely(e, "type", "iconUrl");
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
        updateProjectKey();
        this.description = (String) params.get("description");
        this.type = (String) params.get("type");
		this.priority = (String) params.get("priority");
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
}
