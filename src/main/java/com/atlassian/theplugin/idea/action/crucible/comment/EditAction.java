package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.CommentTreePanel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAboutToUpdate;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentAboutToUpdate;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 3:49:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditAction extends AbstractCommentAction {
    private static final String EDIT_TEXT = "Edit";

    @Override
    public void update(AnActionEvent e) {
        AtlassianTreeNode node = getSelectedNode(e);
        String text = EDIT_TEXT;
        boolean enabled = node != null && checkIfAuthor(node);
        e.getPresentation().setEnabled(enabled);
        if (e.getPlace().equals(CommentTreePanel.MENU_PLACE)) {
            e.getPresentation().setVisible(enabled);
        }
        e.getPresentation().setText(text);
    }

    private boolean isUserAnAuthor(Comment comment, ReviewData review) {
        return review.getServer().getUserName().equals(comment.getAuthor().getUserName());
    }

    public void actionPerformed(AnActionEvent e) {
        Project currentProject = e.getData(DataKeys.PROJECT);
        AtlassianTreeNode node = getSelectedNode(e);
        if (node != null && currentProject != null) {
            editComment(currentProject, node);
        }
    }


    private void editComment(Project project, AtlassianTreeNode treeNode) {
        if (treeNode instanceof GeneralCommentTreeNode) {
            GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
            GeneralComment comment = node.getComment();
            editGeneralComment(project, node.getReview(), comment);
        } else if (treeNode instanceof VersionedCommentTreeNode) {
            VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
            VersionedComment comment = node.getComment();
            editVersionedComment(project, node.getReview(), node.getFile(), comment);
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