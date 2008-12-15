package com.atlassian.theplugin.jira.model;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jgorycki
 * Date: Nov 19, 2008
 * Time: 3:06:58 PM
 */
public class JIRAIssueListModelListenerHolder implements JIRAIssueListModelListener, FrozenModelListener {
	private List<JIRAIssueListModelListener> listeners = new ArrayList<JIRAIssueListModelListener>();
	private List<FrozenModelListener> frozenListeners = new ArrayList<FrozenModelListener>();
	

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
}
