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

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.util.CommentPanelBuilder;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class VersionedCommentTreeNode extends CommentTreeNode {
	private ReviewData review;
	private CrucibleFileInfo file;
	private VersionedComment comment;
	private static final TreeCellRenderer MY_RENDERER = new MyTreeRenderer();

	public VersionedCommentTreeNode(ReviewData review, CrucibleFileInfo file, VersionedComment comment,
            AtlassianClickAction action) {
		super(action);
		this.review = review;
		this.file = file;
		this.comment = comment;
	}

	public VersionedCommentTreeNode(final VersionedCommentTreeNode node) {
		super(node.getAtlassianClickAction());
		this.review = node.review;
		this.file = node.file;
		this.comment = node.comment;
	}

	@Override
    public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public ReviewData getReview() {
		return review;
	}

	public CrucibleFileInfo getFile() {
		return file;
	}

	public VersionedComment getComment() {
		return comment;
	}

	public AtlassianTreeNode getClone() {
		return new VersionedCommentTreeNode(this);
	}

	public int compareTo(Object o) {
		if (o instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode vctn = (VersionedCommentTreeNode) o;
			boolean thisIsFileComment = !(getComment().isFromLineInfo() || getComment().isToLineInfo());
			boolean thatIsFileComment = !(vctn.getComment().isFromLineInfo() || vctn.getComment().isToLineInfo());
			if (thisIsFileComment || thatIsFileComment) {
				if (thisIsFileComment && thatIsFileComment) {
					// that node is also a file comment node, let's sort by date
					return getComment().getCreateDate().compareTo(vctn.getComment().getCreateDate());
				}
				if (thisIsFileComment) {
					return -1;
				}
				return 1;
			}
			// both comments are line comments
			if (getComment().isToLineInfo() && vctn.getComment().isToLineInfo()) {
				return getComment().getToStartLine() - vctn.getComment().getToStartLine();
			}
			if (getComment().isToLineInfo() && vctn.getComment().isFromLineInfo()) {
				return getComment().getToStartLine() - vctn.getComment().getFromStartLine();
			}
			if (getComment().isFromLineInfo() && vctn.getComment().isFromLineInfo()) {
				return getComment().getFromStartLine() - vctn.getComment().getFromStartLine();
			}
			return getComment().getFromStartLine() - vctn.getComment().getToStartLine();
			
		}
		return super.compareTo(o);
	}

	private static class MyTreeRenderer implements TreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) value;
			JPanel panel;
			if (node.isEditable()) {
				panel = CommentPanelBuilder.createEditPanelOfVersionedComment(
						node.getReview(), node.getFile(), node.getComment());
			} else {
				panel = CommentPanelBuilder.createViewPanelOfVersionedComment(
						node.getReview(), node.getFile(), node.getComment(), isSelected);
			}
			return panel;
		}
	}
}
