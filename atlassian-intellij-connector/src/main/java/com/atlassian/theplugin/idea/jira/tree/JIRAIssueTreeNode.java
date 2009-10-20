package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.jira.renderers.JIRAIssueListOrTreeRendererPanel;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;

import javax.swing.*;

public class JIRAIssueTreeNode extends AbstractTreeNode {
	private final JiraIssueAdapter issue;

	public JIRAIssueTreeNode(JiraIssueAdapter issue) {
		super(issue.getKey() + ": " + issue.getSummary(), null, null);
		this.issue = issue;
		renderer = new JIRAIssueListOrTreeRendererPanel(issue);
	}

	private JIRAIssueListOrTreeRendererPanel renderer;

	@Override
	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		renderer.setParameters(selected, c.isEnabled());
		return renderer;
	}

	public JiraIssueAdapter getIssue() {
		return issue;
	}

	@Override
	public String toString() {
		return name;
	}
}
