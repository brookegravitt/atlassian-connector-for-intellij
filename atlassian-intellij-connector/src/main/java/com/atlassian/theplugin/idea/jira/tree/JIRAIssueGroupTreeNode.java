package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;

import javax.swing.*;

public class JIRAIssueGroupTreeNode extends AbstractTreeNode {
	private final JIRAIssueListModel model;

	public JIRAIssueGroupTreeNode(JIRAIssueListModel model, String name, Icon icon, Icon disabledIcon) {
		super(name, icon, disabledIcon);
		this.model = model;
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		return getDefaultRenderer(c, selected, expanded, hasFocus);
	}

	public void onSelect() {
		model.setSeletedIssue(null);
	}

	public String toString() {
		return name;
	}
}
