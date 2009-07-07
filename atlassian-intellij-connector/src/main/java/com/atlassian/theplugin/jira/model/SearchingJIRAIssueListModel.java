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

import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SearchingJIRAIssueListModel extends JIRAIssueListModelListenerHolder {
	private String searchTerm;

	public SearchingJIRAIssueListModel(JIRAIssueListModel parent) {
		super(parent);
		searchTerm = "";
	}

	public void setSearchTerm(@NotNull String searchTerm) {

		if (this.searchTerm.equals(searchTerm)) {
			return;
		}
		this.searchTerm = searchTerm.toLowerCase();
		fireModelChanged();
	}

	public Collection<JIRAIssue> search(Collection<JIRAIssue> col) {
		if (searchTerm.length() == 0) {
			return col;
		}
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();
		for (JIRAIssue i : col) {
			if (isMatch(i)) {
				list.add(i);
			}
		}
		return list;
	}

	private boolean isMatch(JIRAIssue issue) {
		return issue.getKey().toLowerCase().indexOf(searchTerm) > -1
				|| issue.getSummary().toLowerCase().indexOf(searchTerm) > -1;
	}

	public Collection<JIRAIssue> getIssues() {
		return search(parent.getIssues());
	}

	/*
	 * this version of the routine also returns issues that have subtasks matching the search term
	 */
	public Collection<JIRAIssue> getIssuesNoSubtasks() {
		List<JIRAIssue> result = new ArrayList<JIRAIssue>();

		Collection<JIRAIssue> issues = parent.getIssues();
		for (JIRAIssue i : issues) {
			if (!i.isSubTask()) {
				if (isMatch(i)) {
					result.add(i);
				} else {
					for (String subKey : i.getSubTaskKeys()) {
						JIRAIssue sub = parent.findIssue(subKey);
						if (sub != null && isMatch(sub)) {
							result.add(i);
							break;
						}
					}
				}
			}
		}
		return result;
	}

	@NotNull
	public Collection<JIRAIssue> getSubtasks(JIRAIssue p) {
		return search(parent.getSubtasks(p));
	}

	public JIRAIssue findIssue(String key) {
		return parent.findIssue(key);
	}

	public void clearCache() {
	}

	public Set<JIRAIssue> getIssuesCache() {
		return null;
	}

	public JIRAIssue getIssueFromCache(final IssueRecentlyOpenBean recentIssue) {
		return null;
	}

	public void setActiveJiraIssue(final ActiveJiraIssueBean issue) {
	}

	public ActiveJiraIssueBean getActiveJiraIssue() {
		return null;
	}
}
