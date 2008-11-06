package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.jira.api.JIRAIssue;

import javax.swing.tree.DefaultMutableTreeNode;

public class JIRAIssueTreeNode extends DefaultMutableTreeNode {
	private final JIRAIssue issue;

	public JIRAIssueTreeNode(JIRAIssue issue) {
		this.issue = issue;
	}

	public String toString() {
		return issue.getKey() + " " + issue.getSummary();
	}
}
