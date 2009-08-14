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

/**
 * User: kalamon
 * Date: Aug 7, 2009
 * Time: 3:21:40 PM
 */
public class MarkCommentLeaveUnreadAction extends AbstractCommentAction {
    public void actionPerformed(AnActionEvent event) {
        AtlassianTreeNode node = getSelectedNode(event);
        final JTree tree = getTree(event);
        final ReviewDetailsToolWindow panel = IdeaHelper.getReviewDetailsToolWindow(event);
        if (panel != null && node != null && (node instanceof CommentTreeNode)) {
            final CommentTreeNode commentNode = (CommentTreeNode) node;
            Task.Modal task = new Task.Modal(IdeaHelper.getCurrentProject(event), "Leaving comment unread", true) {
                private Throwable error = null;
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        commentNode.getReview().markCommentLeaveUnread(commentNode.getComment());
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
                                panel.getAtlassianTreeWithToolbar(), "Leaving comment unread failed", error);
                    }
                }
            };
            ProgressManager.getInstance().run(task);
        }
    }

    @Override
    public void update(AnActionEvent event) {
        boolean enabled = false;
        AtlassianTreeNode node = getSelectedNode(event);
        if (node != null && (node instanceof CommentTreeNode)) {
            CommentTreeNode n = (CommentTreeNode) node;

            Comment comment = n.getComment();
            Comment.ReadState currentState = comment.getReadState();
            if (!comment.getAuthor().getUsername().equals(n.getReview().getServerData().getUsername())
                    && currentState != Comment.ReadState.UNKNOWN
                    && currentState != Comment.ReadState.LEAVE_UNREAD) {
                enabled = true;
            }
        }
        event.getPresentation().setEnabled(enabled);
        event.getPresentation().setVisible(enabled);
    }
}
