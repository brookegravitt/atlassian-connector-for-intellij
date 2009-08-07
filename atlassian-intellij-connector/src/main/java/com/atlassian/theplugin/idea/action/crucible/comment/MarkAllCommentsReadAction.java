package com.atlassian.theplugin.idea.action.crucible.comment;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.crucible.ReviewDetailsToolWindow;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.CommentBean;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;

/**
 * User: kalamon
 * Date: Aug 7, 2009
 * Time: 2:11:15 PM
 */
public class MarkAllCommentsReadAction extends AbstractCommentAction {
    public void actionPerformed(AnActionEvent event) {
        final ReviewDetailsToolWindow panel = IdeaHelper.getReviewDetailsToolWindow(event);
        PermId reviewId = null;
        ServerData serverData = null;
        if (panel != null && panel.getReview() != null) {
            serverData = panel.getReview().getServerData();
            reviewId = panel.getReview().getPermId();
        }
        if (reviewId == null || serverData == null) {
            return;
        }
        final PermId reviewIdFinal = reviewId;
        final ServerData serverDataFinal = serverData;
        final JTree tree = getTree(event);
        Task.Modal task = new Task.Modal(IdeaHelper.getCurrentProject(event), "Marking all comments as read", true) {
            Throwable error = null;
            public void run(@NotNull ProgressIndicator progressIndicator) {
                CrucibleServerFacade f = IntelliJCrucibleServerFacade.getInstance();

                try {
                    f.markAllCommentsRead(serverDataFinal, reviewIdFinal);
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
                } else {
                    traverseCommentsInTree(tree, new TraverseTreeListener() {
                        public boolean execute(CommentTreeNode node) {
                            // todo: fime? Shouldn't we be doing this on the model itself? Not sure
                            if (node.getComment().getReadState() == Comment.ReadState.UNREAD) {
                                ((CommentBean) node.getComment()).setReadState(Comment.ReadState.READ);
                            }
                            ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                            return false;
                        }
                    });
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
