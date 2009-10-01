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

package com.atlassian.theplugin.commons.jira.api.commons;

import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRACommentBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRASecurityLevelBean;
import com.atlassian.theplugin.commons.jira.api.commons.soap.axis.RemoteIssue;
import org.jdom.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JIRAIssueBean implements JIRAIssue {
	private Long id;
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
	private JIRAPriorityBean priorityConstant;
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

	private List<String> subTaskList;
	private boolean thisIsASubTask;
	private String parentIssueKey;
	private String originalEstimate;
	private String remainingEstimate;
	private String timeSpent;
	private List<JIRAComment> commentsList;
	private Object rawSoapIssue;
	private String originalEstimateInSeconds;
	private String remainingEstimateInSeconds;
	private String timeSpentInSeconds;
	private JIRASecurityLevelBean securityLevel;
    private String environment;

    public JIRAIssueBean() {
	}

	public JIRAIssueBean(JIRAIssue issue) {
		id = issue.getId();
		key = issue.getKey();
		summary = issue.getSummary();
		status = issue.getStatus();
        environment = issue.getEnvironment();
		statusUrl = issue.getStatusTypeUrl();
		type = issue.getType();
		typeUrl = issue.getTypeIconUrl();
		priority = issue.getPriority();
		priorityUrl = issue.getPriorityIconUrl();
		description = issue.getDescription();
		projectKey = issue.getProjectKey();
		statusConstant = issue.getStatusConstant();
		typeConstant = issue.getTypeConstant();
		priorityConstant = issue.getPriorityConstant();
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
		thisIsASubTask = issue.isSubTask();
		subTaskList = issue.getSubTaskKeys();
		parentIssueKey = issue.getParentIssueKey();
		originalEstimate = issue.getOriginalEstimate();
		originalEstimateInSeconds = issue.getOriginalEstimateInSeconds();
		remainingEstimate = issue.getRemainingEstimate();
		remainingEstimateInSeconds = issue.getRemainingEstimateInSeconds();
		timeSpent = issue.getTimeSpent();
		timeSpentInSeconds = issue.getTimeSpentInSeconds();
        serverUrl = issue.getServerUrl();
        rawSoapIssue = issue.getRawSoapIssue();
	}

	public JIRAIssueBean(String serverUrl, Element e) {
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
		this.priority = getTextSafety(e, "priority", "Unknown");
		this.priorityUrl = getAttributeSafely(e, "priority", "iconUrl");
		try {
			this.priorityId = Long.parseLong(getAttributeSafely(e, "priority", "id"));
		} catch (NumberFormatException ex) {
			this.priorityId = 0;
		}
		this.description = getTextSafely(e, "description");
        this.environment = getTextSafely(e, "environment");
        
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

		this.parentIssueKey = getTextSafely(e, "parent");
		this.thisIsASubTask = parentIssueKey != null;
		subTaskList = new ArrayList<String>();
		Element subtasks = e.getChild("subtasks");
		if (subtasks != null) {
			for (Object subtask : subtasks.getChildren("subtask")) {
				String subTaskKey = ((Element) subtask).getText();
				if (subTaskKey != null) {
					subTaskList.add(subTaskKey);
				}
			}
		}

		this.originalEstimate = getTextSafely(e, "timeoriginalestimate");
		this.remainingEstimate = getTextSafely(e, "timeestimate");
		this.timeSpent = getTextSafely(e, "timespent");
		this.originalEstimateInSeconds = getAttributeSafely(e, "timeoriginalestimate", "seconds");
		this.remainingEstimateInSeconds = getAttributeSafely(e, "timeestimate", "seconds");
		this.timeSpentInSeconds = getAttributeSafely(e, "timespent", "seconds");
        this.serverUrl = serverUrl;

		Element comments = e.getChild("comments");
		if (comments != null) {
			commentsList = new ArrayList<JIRAComment>();
			for (Object comment : comments.getChildren("comment")) {
				Element el = (Element) comment;
				String commentId = el.getAttributeValue("id", "-1");
				String author = el.getAttributeValue("author", "Unknown");
				String text = el.getText();
				String creationDate = el.getAttributeValue("created", "Unknown");

				Calendar cal = Calendar.getInstance();
				DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", Locale.US);
				try {
					cal.setTime(df.parse(creationDate));
				} catch (java.text.ParseException ex) {
					// oh well, invalid time  - now what? :(
				}

				commentsList.add(new JIRACommentBean(commentId, author, text, cal));
			}
		}
	}

    public JIRAIssueBean(String url, RemoteIssue remoteIssue) {
        this.serverUrl = url;
        id = Long.valueOf(remoteIssue.getId());
		key = remoteIssue.getKey();
		summary = remoteIssue.getSummary();
		status = remoteIssue.getStatus();
        environment = remoteIssue.getEnvironment();
		//statusUrl
		type = remoteIssue.getType();
		//typeUrl = remoteIssue.getTypeIconUrl();
		priority = remoteIssue.getPriority();
		//priorityUrl = remoteIssue.getPriorityIconUrl();
		description = remoteIssue.getDescription();
		projectKey = remoteIssue.getProject();
		//statusConstant
		//typeConstant
		//priorityConstant = remoteIssue.getPriorityConstant();
		assignee = remoteIssue.getAssignee();
		//assigneeId = remoteIssue.getAssigneeId();
		reporter = remoteIssue.getReporter();
		//reporterId = issue.getReporterId();
		resolution = remoteIssue.getResolution();
		//created = remoteIssue.getCreated();
		//updated = remoteIssue.getUpdated();
		//statusId = remoteIssue.getStatusId();
		//priorityId = issue.getPriorityId();
		//typeId = issue.getTypeId();
		//thisIsASubTask = remoteIssue.isSubTask();
		//subTaskList = remoteIssue.getSubTaskKeys();
		//parentIssueKey = remoteIssue.getParentIssueKey();
		//originalEstimate = remoteIssue.getOriginalEstimate();
		//originalEstimateInSeconds = remoteIssue.getOriginalEstimateInSeconds();
		//remainingEstimate = remoteIssue.getRemainingEstimate();
		//remainingEstimateInSeconds = remoteIssue.getRemainingEstimateInSeconds();
		//timeSpent = remoteIssue.getTimeSpent();
		//timeSpentInSeconds = issue.getTimeSpentInSeconds();

    }



    public JIRAPriorityBean getPriorityConstant() {
		return priorityConstant;
	}

	public void setPriority(JIRAPriorityBean priority) {
		this.priority = priority.getName();
		this.priorityConstant = priority;
	}

	public JIRAIssueBean(Map params) {
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

    private String getTextSafety(Element e, String name, String defaultName) {
        String text = getTextSafely(e, name);

        return text != null ? text : defaultName;
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

	public String getProjectUrl() {
		return getServerUrl() + "/browse/" + getProjectKey();
	}

	public String getIssueUrl() {
		return getServerUrl() + "/browse/" + getKey();
	}


	public Long getId() {
		return id;
	}

	public boolean isSubTask() {
		return thisIsASubTask;
	}

	public String getParentIssueKey() {
		return parentIssueKey;
	}

	public List<String> getSubTaskKeys() {
		return subTaskList;
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

    public String getEnvironment() {
        return environment;
    }

    public String getType() {
		return type;
	}

	public String getTypeIconUrl() {
		return typeUrl;
	}

	public void setTypeIconUrl(String newTypeUrl) {
		this.typeUrl = newTypeUrl;
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

		return true;
	}

	private static final int ONE_EFF = 31;

	public int hashCode() {
		int result = 0;

		result = ONE_EFF * result + (key != null ? key.hashCode() : 0);
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

	public String getOriginalEstimate() {
		return originalEstimate;
	}

	public void setOriginalEstimate(String originalEstimate) {
		this.originalEstimate = originalEstimate;
	}

	public String getOriginalEstimateInSeconds() {
		return originalEstimateInSeconds;
	}

	public String getRemainingEstimate() {
		return remainingEstimate;
	}

	public String getRemainingEstimateInSeconds() {
		return remainingEstimateInSeconds;
	}

	public void setRemainingEstimate(String remainingEstimate) {
		this.remainingEstimate = remainingEstimate;
	}

	public String getTimeSpent() {
		return timeSpent;
	}

	public String getTimeSpentInSeconds() {
		return timeSpentInSeconds;
	}

	public void setTimeSpent(String timeSpent) {
		this.timeSpent = timeSpent;
	}

	public List<JIRAComment> getComments() {
		return commentsList;
	}

    public Object getRawSoapIssue() {
		return rawSoapIssue;
	}

	public void setRawSoapIssue(Object soapIssue) {
		rawSoapIssue = soapIssue;
	}

	public JIRASecurityLevelBean getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(final JIRASecurityLevelBean securityLevelBean) {
		this.securityLevel = securityLevelBean;
	}
}
