package com.atlassian.theplugin.idea.ui.tree.comment;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 11:19:24 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CommentTreeNode extends AtlassianTreeNode {
	private boolean editable = false;

	public void setEditable(boolean isEditable) {
		editable = isEditable;
	}

	public boolean isEditable() {
		return editable;
	}
}
