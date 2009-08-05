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

package com.atlassian.theplugin.idea.ui.tree.comment;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.idea.crucible.ui.ReviewCommentRenderer;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.file.FileNode;

import javax.swing.tree.TreeCellRenderer;

public abstract class CommentTreeNode extends FileNode {
	private boolean editable;
	private static final TreeCellRenderer MY_RENDERER = new ReviewCommentRenderer();

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setExpanded(final boolean expanded) {
		if (isExpanded != expanded) {
			isExpanded = expanded;
		}
	}
	public abstract Comment getComment();

	private boolean isExpanded;
	protected ReviewAdapter review;

	protected CommentTreeNode(AtlassianClickAction action) {
		super("comment", action);
	}

	public void setEditable(boolean isEditable) {
		editable = isEditable;
	}

	public boolean isEditable() {
		return editable;
	}

	public ReviewAdapter getReview() {
		return review;
	}

	@Override
	public boolean isCompactable() {
		return false;
	}

	@Override
    public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	@Override
	public String toString() {
		return "Comment node: " + getComment().getMessage();
	}
}
