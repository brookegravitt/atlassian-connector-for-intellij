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

package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewDetailsToolWindow;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

public abstract class AbstractCommentAction extends AnAction {

	@Nullable
	protected JTree getTree(AnActionEvent e) {

		DataContext dataContext = e.getDataContext();
		Component component = null;
		AtlassianTreeWithToolbar twtb = (AtlassianTreeWithToolbar) dataContext.getData(Constants.FILE_TREE);
		if (twtb == null) {
			ReviewDetailsToolWindow ctw = IdeaHelper.getReviewDetailsToolWindow(e);
			if (ctw != null) {
				twtb = ctw.getAtlassianTreeWithToolbar();
			}
		}
		if (twtb != null) {
			component = twtb.getTreeComponent();
		}

		if (component == null) {
			return null;
		}

		return (JTree) component;
	}

	@Nullable
	private TreePath getSelectedTreePath(AnActionEvent e) {
		JTree tree = getTree(e);
		if (tree != null) {
			return tree.getSelectionPath();
		}
		return null;
	}

	@Nullable
	protected AtlassianTreeNode getSelectedNode(AnActionEvent e) {
		TreePath treepath = getSelectedTreePath(e);
		if (treepath == null) {
			return null;
		}
		return getSelectedNode(treepath);
	}

	private AtlassianTreeNode getSelectedNode(TreePath path) {
		Object o = path.getLastPathComponent();
		if (o instanceof AtlassianTreeNode) {
			return (AtlassianTreeNode) o;
		}
		return null;
	}

	protected boolean checkIfAuthor(final AtlassianTreeNode node) {
		if (node == null) {
			return false;
		}
		boolean result = false;
		if (node instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode anode = (VersionedCommentTreeNode) node;
			if (isUserAnAuthor(anode.getComment(), anode.getReview())) {
				result = true;
			}
		}
		if (node instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode anode = (GeneralCommentTreeNode) node;
			if (isUserAnAuthor(anode.getComment(), anode.getReview())) {
				result = true;
			}
		}
		return result;
	}

	protected boolean checkIfDraftAndAuthor(final AtlassianTreeNode node) {
		if (node == null) {
			return false;
		}
		boolean result = false;
		if (node instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode anode = (VersionedCommentTreeNode) node;
			if (isUserAnAuthor(anode.getComment(), anode.getReview())
					&& anode.getComment().isDraft()) {
				result = true;
			}
		}
		if (node instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode anode = (GeneralCommentTreeNode) node;
			if (isUserAnAuthor(anode.getComment(), anode.getReview())
					&& anode.getComment().isDraft()) {
				result = true;
			}
		}
		return result;
	}

	private boolean isUserAnAuthor(Comment comment, ReviewAdapter review) {
		return review.getServerData().getUserName().equals(comment.getAuthor().getUserName());
	}
}
