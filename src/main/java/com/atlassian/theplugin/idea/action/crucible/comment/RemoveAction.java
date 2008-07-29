package com.atlassian.theplugin.idea.action.crucible.comment;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Icons;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.CommentTreePanel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentAboutToUpdate;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAboutToUpdate;
import com.atlassian.theplugin.idea.crucible.events.CommentAboutToRemove;
import com.atlassian.theplugin.commons.crucible.api.model.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 3:50:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveAction extends AbstractCommentAction {
    private static final String REMOVE_TEXT = "Remove";

    @Override
    public void update(AnActionEvent e) {
        AtlassianTreeNode node = getSelectedNode(e);
        String text = REMOVE_TEXT;
        boolean enabled = node != null && checkIfAuthor(node);
        e.getPresentation().setEnabled(enabled);
        if (e.getPlace().equals(CommentTreePanel.MENU_PLACE)) {
            e.getPresentation().setVisible(enabled);
        }
        e.getPresentation().setText(text);
    }

    public void actionPerformed(AnActionEvent e) {
        Project currentProject = e.getData(DataKeys.PROJECT);
        AtlassianTreeNode node = getSelectedNode(e);
        if (node != null && currentProject != null) {
            removeComment(currentProject, node);
        }
    }


    private void removeComment(Project project, AtlassianTreeNode treeNode) {
        Comment comment = null;
        ReviewData review = null;

        if (treeNode instanceof GeneralCommentTreeNode) {
            GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
            comment = node.getComment();
            review = node.getReview();
        } else if (treeNode instanceof VersionedCommentTreeNode) {
            VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
            comment = node.getComment();
            review = node.getReview();
        }
        if (comment == null || review == null) {
            return;
        }
        removeComment(project, review, comment);
    }

    private void removeComment(final Project project, final ReviewData review, final Comment comment) {
        int result = Messages.showYesNoDialog(project, "Are you sure you want remove your comment?", "Confirmation required",
                Icons.TASK_ICON);
        if (result == DialogWrapper.OK_EXIT_CODE) {
            IdeaHelper.getReviewActionEventBroker().trigger(
                    new CommentAboutToRemove(CrucibleReviewActionListener.ANONYMOUS,
                            review, comment));
        }
    }

    private void editGeneralComment(final Project project, final ReviewData review, final GeneralComment comment) {
        List<CustomFieldDef> metrics = new ArrayList<CustomFieldDef>();
        CommentEditForm dialog = new CommentEditForm(project, review, (CommentBean) comment, metrics);
        dialog.pack();
        dialog.setModal(true);
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            IdeaHelper.getReviewActionEventBroker().trigger(
                    new GeneralCommentAboutToUpdate(CrucibleReviewActionListener.ANONYMOUS,
                            review, comment));
        }
    }


    private void editVersionedComment(Project project, ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
        List<CustomFieldDef> metrics = new ArrayList<CustomFieldDef>();
        CommentEditForm dialog = new CommentEditForm(project, review, (CommentBean) comment, metrics);
        dialog.pack();
        dialog.setModal(true);
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            IdeaHelper.getReviewActionEventBroker().trigger(
                    new VersionedCommentAboutToUpdate(CrucibleReviewActionListener.ANONYMOUS,
                            review, file, comment));
        }
    }
}
