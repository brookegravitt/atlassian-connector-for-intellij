package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class JIRAIssueListModelImpl extends JIRAIssueListModelListenerHolder implements JIRAIssueListModel {

	private Map<String, JIRAIssue> issues;

	private JIRAIssue selectedIssue;
	private boolean frozen = false;

	private JIRAIssueListModelImpl() {
		issues = new HashMap<String, JIRAIssue>();
	}

	public static JIRAIssueListModel createInstance() {
		return new JIRAIssueListModelImpl();
	}

	public void clear() {
		issues.clear();
	}

	public void addIssue(JIRAIssue issue) {
		issues.put(issue.getKey(), issue);
	}

	public void addIssues(Collection<JIRAIssue> list) {
		for (JIRAIssue i : list) {
			addIssue(i);
		}
	}

	public void setIssue(JIRAIssue issue) {
		issues.put(issue.getKey(), issue);
		if (selectedIssue != null && selectedIssue.getKey().equals(issue.getKey())) {
			selectedIssue  = issue;
		}
	}

	public Collection<JIRAIssue> getIssues() {
		return issues.values();
	}

	public void fireModelChanged() {
		modelChanged(this);
	}

	public void fireIssuesLoaded(int numberOfLoadedIssues) {
		issuesLoaded(this, numberOfLoadedIssues);
	}

	public void addModelListener(JIRAIssueListModelListener listener) {
		addListener(listener);
	}

	public void removeModelListener(JIRAIssueListModelListener listener) {
		removeListener(listener);
	}

	public boolean isModelFrozen() {
		return this.frozen;
	}

	public void setModelFrozen(boolean frozen) {
		this.frozen = frozen;
		modelFrozen(this, this.frozen);
	}

	public void setSeletedIssue(JIRAIssue issue) {
		if (issue != null && issues.containsValue(issue)) {
			selectedIssue = issue;
		} else {
			selectedIssue = null;
		}
	}

	public JIRAIssue getSelectedIssue() {
		return selectedIssue;
	}
}
