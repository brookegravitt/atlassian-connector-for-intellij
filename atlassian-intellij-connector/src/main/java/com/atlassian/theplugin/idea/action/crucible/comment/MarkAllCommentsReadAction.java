package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewDetailsToolWindow;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * User: kalamon
 * Date: Aug 7, 2009
 * Time: 2:11:15 PM
 */
public class MarkAllCommentsReadAction extends AbstractCommentAction {
    public void actionPerformed(AnActionEvent event) {
        final ReviewDetailsToolWindow panel = IdeaHelper.getReviewDetailsToolWindow(event);
        if (panel == null || panel.getReview() == null) {
            return;
        }

        Task.Modal task = new Task.Modal(IdeaHelper.getCurrentProject(event), "Marking all comments as read", true) {
            private Throwable error = null;
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    panel.getReview().markAllCommentsRead();
                } catch (RemoteApiException e) {
                    error = e;
                } catch (ServerPasswordNotProvidedException e) {
                    error = e;
                }
            }

            @Override
            public void onSuccess() {
                if (error != null) {
                    DialogWithDetails.showExceptionDialog(
                            panel.getAtlassianTreeWithToolbar(), "Marking all comments as read failed", error);
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    @Override
    public void update(AnActionEvent event) {
        final boolean[] enabled = new boolean[]{false};
        traverseCommentsInTree(getTree(event), new TraverseTreeListener() {
            public boolean execute(CommentTreeNode node) {
                if (node.getComment().getReadState() == Comment.ReadState.UNREAD
                    || node.getComment().getReadState() == Comment.ReadState.LEAVE_UNREAD) {
                    enabled[0] = true;
                    return true;
                }
                return false;
            }
        });
        event.getPresentation().setEnabled(enabled[0]);
    }

    private void traverseCommentsInTree(JTree tree, TraverseTreeListener l) {
        if (tree == null) {
            return;
        }
        DefaultMutableTreeNode start = (DefaultMutableTreeNode) tree.getModel().getRoot();
        if (start == null) {
            return;
        }
        AtlassianTreeNode n = (AtlassianTreeNode) start.getNextNode();
        while (n != null) {
            if (n instanceof CommentTreeNode) {
                CommentTreeNode ctn = (CommentTreeNode) n;
                if (l.execute(ctn)) {
                    break;
                }
            }
            n = (AtlassianTreeNode) n.getNextNode();
        }
    }

    private interface TraverseTreeListener {
        boolean execute(CommentTreeNode node);
    }
}
