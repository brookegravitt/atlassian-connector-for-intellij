package com.atlassian.theplugin.idea.jira.ui;

import com.atlassian.theplugin.jira.api.JIRAComment;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * User: jgorycki
 * Date: Jan 30, 2009
 * Time: 1:48:29 PM
 */
public class IssueCommentTreeNode extends DefaultMutableTreeNode {
	private final JIRAComment comment;
	private boolean expanded;
	private int nr;

	public IssueCommentTreeNode(JIRAComment comment, int nr) {
		this.comment = comment;
		this.nr = nr;
	}

	public JIRAComment getComment() {
		return comment;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public int getCommentNr() {
		return nr;
	}
}
