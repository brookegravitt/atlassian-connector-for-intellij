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
package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.idea.jira.tree.JIRAIssueTreeNode;
import com.atlassian.theplugin.idea.ui.tree.AbstractTree;
import com.atlassian.theplugin.jira.api.JIRAIssue;

import javax.swing.tree.TreePath;

/**
 * @author Jacek Jaroczynski
 */
public class JiraIssueListTree extends AbstractTree {
	public JIRAIssue getSelectedIssue() {
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null && selectionPath.getLastPathComponent() instanceof JIRAIssueTreeNode) {
			return ((JIRAIssueTreeNode) selectionPath.getLastPathComponent()).getIssue();
		} else {
			// nothing selected
			return null;
		}
	}
}
