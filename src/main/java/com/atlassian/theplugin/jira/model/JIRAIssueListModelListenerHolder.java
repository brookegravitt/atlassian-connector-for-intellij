package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

/**
 * User: jgorycki
 * Date: Nov 19, 2008
 * Time: 3:06:58 PM
 */
public abstract class JIRAIssueListModelListenerHolder 
		implements JIRAIssueListModelListener, FrozenModelListener, JIRAIssueListModel {

	private List<JIRAIssueListModelListener> listeners = new ArrayList<JIRAIssueListModelListener>();
	private List<FrozenModelListener> frozenListeners = new ArrayList<FrozenModelListener>();
	protected final JIRAIssueListModel parent;

	public JIRAIssueListModelListenerHolder(JIRAIssueListModel parent) {
		this.parent = parent;
		if (parent != null) {
			parent.addModelListener(this);
		}
	}

	protected void addListener(JIRAIssueListModelListener l) {
		listeners.add(l);
	}

	protected void removeListener(JIRAIssueListModelListener l) {
		listeners.remove(l);
	}

	public void modelChanged(JIRAIssueListModel model) {
		for (JIRAIssueListModelListener l : listeners) {
			l.modelChanged(model);
		}
	}

	public void issuesLoaded(JIRAIssueListModel model, int loadedIssues) {
		for (JIRAIssueListModelListener l : listeners) {
			l.issuesLoaded(model, loadedIssues);
		}
	}

	public void modelFrozen(FrozenModel model, boolean frozen) {
		for (FrozenModelListener l : frozenListeners) {
			l.modelFrozen(model, frozen);
		}
	}

	public void addFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.add(listener);
	}

	public void removeFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.remove(listener);
	}

	public void addModelListener(JIRAIssueListModelListener listener) {
		addListener(listener);
	}

	public void removeModelListener(JIRAIssueListModelListener listener) {
		removeListener(listener);
	}

	public void clear() {
		if (parent != null) {
			parent.clear();
		}
	}

	public void addIssue(JIRAIssue issue) {
		if (parent != null) {
			parent.addIssue(issue);
		}
	}

	public void addIssues(Collection<JIRAIssue> issues) {
		if (parent != null) {
			parent.addIssues(issues);
		}
	}

	public void setSeletedIssue(JIRAIssue issue) {
		if (parent != null) {
			parent.setSeletedIssue(issue);
		}
	}

	public JIRAIssue getSelectedIssue() {
		if (parent != null) {
			JIRAIssue i = parent.getSelectedIssue();
			if (getIssues().contains(i)) {
				return i;
			}
		}
		return null;
	}

	public void setIssue(JIRAIssue issue) {
		if (parent != null) {
			parent.setIssue(issue);
		}
	}

	public void fireModelChanged() {
		modelChanged(this);
	}

	public void fireIssuesLoaded(int numberOfLoadedIssues) {
		issuesLoaded(this, numberOfLoadedIssues);
	}

	public boolean isModelFrozen() {
		if (parent != null) {
			return parent.isModelFrozen();
		}
		return false;
	}

	public void setModelFrozen(boolean frozen) {
		if (parent != null) {
			parent.setModelFrozen(frozen);
		}
	}
}
