package com.atlassian.theplugin.idea.action.crucible.comment;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import javax.swing.*;
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
	private TreePath getSelectedTreePath(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		Component component = DataKeys.CONTEXT_COMPONENT.getData(dataContext);
		if (!(component instanceof JTree)) {
			return null;
		}
		final JTree theTree = (JTree) component;
		return theTree.getSelectionPath();
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

    private boolean isUserAnAuthor(Comment comment, ReviewData review) {
        return review.getServer().getUserName().equals(comment.getAuthor().getUserName());
    }
}
