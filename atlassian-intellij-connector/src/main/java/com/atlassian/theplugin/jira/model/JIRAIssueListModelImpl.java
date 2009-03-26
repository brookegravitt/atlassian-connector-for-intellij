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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class JIRAIssueListModelImpl extends JIRAIssueListModelListenerHolder implements JIRAIssueListModel, FrozenModel {

	private Set<JIRAIssue> issues;

	private JIRAIssue selectedIssue;
	private boolean modelFrozen = false;

	private JIRAIssueListModelImpl() {
		super(null);
		issues = new HashSet<JIRAIssue>();
	}

	public static JIRAIssueListModel createInstance() {
		return new JIRAIssueListModelImpl();
	}

	public void clear() {
		issues.clear();
	}

	public void addIssue(JIRAIssue issue) {
		issues.add(issue);
	}

	public void addIssues(Collection<JIRAIssue> list) {
		for (JIRAIssue i : list) {
			addIssue(i);
		}
	}

	public void setIssue(JIRAIssue issue) {
		if (issue != null) {
			issues.add(issue);
			if (selectedIssue != null && selectedIssue.getKey().equals(issue.getKey())) {
				selectedIssue = issue;
			}
		}
	}

	public Collection<JIRAIssue> getIssues() {
		return issues;
	}

	public Collection<JIRAIssue> getIssuesNoSubtasks() {
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();

		for (JIRAIssue i : issues) {
			if (!i.isSubTask()) {
				list.add(i);
			}
		}
		return list;
	}

	@NotNull
	public Collection<JIRAIssue> getSubtasks(JIRAIssue parent) {
		if (parent == null) {
			return getSubtasksWithMissingParents();
		}

		List<JIRAIssue> list = new ArrayList<JIRAIssue>();

		for (String key : parent.getSubTaskKeys()) {
			JIRAIssue sub = findIssue(key);
			if (sub != null) {
				list.add(sub);
			}
		}
		return list;
	}

	@NotNull
	private Collection<JIRAIssue> getSubtasksWithMissingParents() {
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();

		for (JIRAIssue i : getIssues()) {
			if (i.isSubTask()) {
				if (findIssue(i.getParentIssueKey()) == null) {
					list.add(i);
				}
			}
		}
		return list;
	}

	public JIRAIssue findIssue(String key) {
		for (JIRAIssue issue : issues) {
			if (issue.getKey().equals(key)) {
				return issue;
			}
		}
		return null;
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

	private void fireModelFrozen() {
		modelFrozen(this, this.modelFrozen);
	}

	public void setSeletedIssue(JIRAIssue issue) {
		if (issue != null && issues.contains(issue)) {
			selectedIssue = issue;
		} else {
			selectedIssue = null;
		}
	}

	public JIRAIssue getSelectedIssue() {
		return selectedIssue;
	}
}
