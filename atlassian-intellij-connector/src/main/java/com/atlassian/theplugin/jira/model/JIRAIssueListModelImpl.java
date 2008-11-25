package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.*;

public final class JIRAIssueListModelImpl extends JIRAIssueListModelListenerHolder implements JIRAIssueListModel, FrozenModel {

	private Map<String, JIRAIssue> issues;

	private JIRAIssue selectedIssue;
	private boolean modelFrozen = false;

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

	public Collection<JIRAIssue> getIssuesNoSubtasks() {
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();

		for (JIRAIssue i : issues.values()) {
			if (!i.isSubTask()) {
				list.add(i);
			}
		}
		return list;
	}

	public Collection<JIRAIssue> getSubtasks(JIRAIssue parent) {
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();

		for (JIRAIssue i : getIssues()) {
			for (String key : parent.getSubTaskKeys()) {
				if (key.equals(i.getKey())) {
					list.add(i);
					break;
				}
			}
		}
		return list;
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
		return this.modelFrozen;
	}

	public void setModelFrozen(boolean frozen) {
		this.modelFrozen = frozen;
		fireModelFrozen();
	}

	public void addFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.add(listener);
	}

	public void removeFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.remove(listener);
	}

	private void fireModelFrozen() {
		modelFrozen(this, this.modelFrozen);
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
