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
package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	public void issueUpdated(JIRAIssue issue) {
		for (JIRAIssueListModelListener l : listeners) {
			l.issueUpdated(issue);
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
		if (parent != null) {
			parent.addFrozenModelListener(listener);
		} else {
			frozenListeners.add(listener);
		}
	}

	public void removeFrozenModelListener(FrozenModelListener listener) {
		if (parent != null) {
			parent.addFrozenModelListener(listener);
		} else {
			frozenListeners.remove(listener);
		}
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

//	public void addIssue(JIRAIssue issue) {
//		if (parent != null) {
//			parent.addIssue(issue);
//		}
//	}

	public void addIssues(Collection<JIRAIssue> issues) {
		if (parent != null) {
			parent.addIssues(issues);
		}
	}

//	public void setSeletedIssue(JIRAIssue issue) {
//		if (parent != null) {
//			parent.setSeletedIssue(issue);
//		}
//	}

	public void updateIssue(JIRAIssue issue) {
		if (parent != null) {
			parent.updateIssue(issue);
		}
	}

	public void fireModelChanged() {
		modelChanged(this);
	}

	public void fireIssueUpdated(final JIRAIssue issue) {
		issueUpdated(issue);
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
