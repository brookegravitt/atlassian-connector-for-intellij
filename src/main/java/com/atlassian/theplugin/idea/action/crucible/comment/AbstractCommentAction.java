package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
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

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 29, 2008
 * Time: 10:42:11 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCommentAction extends AnAction {

	@Nullable
	protected JTree getTree(AnActionEvent e) {

		DataContext dataContext = e.getDataContext();
		Component component = (AtlassianTree) dataContext.getData(Constants.CRUCIBLE_COMMENT_TREE);
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

	private boolean isUserAnAuthor(Comment comment, ReviewData review) {
        return review.getServer().getUserName().equals(comment.getAuthor().getUserName());
    }
}
