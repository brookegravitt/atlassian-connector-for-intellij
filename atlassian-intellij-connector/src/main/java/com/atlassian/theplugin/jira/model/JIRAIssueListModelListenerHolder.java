package com.atlassian.theplugin.jira.model;

import java.util.List;
import java.util.ArrayList;

/**
 * User: jgorycki
 * Date: Nov 19, 2008
 * Time: 3:06:58 PM
 */
public class JIRAIssueListModelListenerHolder implements JIRAIssueListModelListener {
	private List<JIRAIssueListModelListener> listeners = new ArrayList<JIRAIssueListModelListener>();

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
}
