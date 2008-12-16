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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
			if (i.getKey().toLowerCase().indexOf(searchTerm) > -1
					|| i.getSummary().toLowerCase().indexOf(searchTerm) > -1) {
				list.add(i);
			}
		}
		return list;
	}

	public Collection<JIRAIssue> getIssues() {
		return search(parent.getIssues());
	}

	public Collection<JIRAIssue> getIssuesNoSubtasks() {
		return search(parent.getIssuesNoSubtasks());
	}

	public Collection<JIRAIssue> getSubtasks(JIRAIssue p) {
		return search(parent.getSubtasks(p));
	}
}
