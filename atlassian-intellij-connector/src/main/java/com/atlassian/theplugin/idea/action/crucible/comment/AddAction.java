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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanel;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanelWithRunners;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.FileNameNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralSectionNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import java.util.Date;

public class AddAction extends AbstractCommentAction {
	private static final String REPLY_TEXT = "Reply";
	private static final String COMMENT_TEXT = "Add Comment";
	private static final String FILE_COMMENT_TEXT = "Add Revision Comment";

	@Override
	public void update(AnActionEvent e) {
		AtlassianTreeNode node = getSelectedNode(e);

		String text = COMMENT_TEXT;
		boolean enabled = node != null && checkIfAuthorized(getReview(node));

		if (enabled) {
			if (node instanceof CrucibleFileNode) {
                text = FILE_COMMENT_TEXT;
			} else if (node instanceof VersionedCommentTreeNode || node instanceof GeneralCommentTreeNode) {
				text = REPLY_TEXT;
			}
		}
		e.getPresentation().setEnabled(enabled);
		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
		e.getPresentation().setText(text);
	}

    @Override
    public void beforeActionPerformedUpdate(AnActionEvent event) {
        Editor editor = event.getData(DataKeys.EDITOR);
        event.getPresentation().setEnabled(editor == null);
    }

	private boolean checkIfAuthorized(final ReviewAdapter review) {
		if (review == null) {
			return false;
		}
		if (!review.getActions().contains(CrucibleAction.COMMENT)) {
			return false;
		}
		return true;
	}

	@Nullable
	private ReviewAdapter getReview(final AtlassianTreeNode node) {
		if (node instanceof CommentTreeNode) {
			final CommentTreeNode cNode = (CommentTreeNode) node;
			return cNode.getReview();
		} else if (node instanceof GeneralSectionNode) {
			return ((GeneralSectionNode) node).getReview();
		} else if (node instanceof FileNameNode) {
			return ((FileNameNode) node).getReview();
		} else if (node instanceof CrucibleFileNode) {
			return ((CrucibleFileNode) node).getReview();
		}
		return null;
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode node = getSelectedNode(e);
		if (node != null) {
            e.getPresentation().putClientProperty(CommentTooltipPanel.JBPOPUP_PARENT_COMPONENT, getTree(e));
			addComment(e, node);
		}
	}

	private void addComment(AnActionEvent event, AtlassianTreeNode treeNode) {
		if (treeNode instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
			addReplyToGeneralComment(event, node.getReview(), node.getComment());
		} else if (treeNode instanceof FileNameNode) {
			FileNameNode node = (FileNameNode) treeNode;
            addCommentToFile(event, node.getReview(), node.getFile());
		} else if (treeNode instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
			addReplyToGeneralComment(event, node.getReview(), node.getComment());
		} else if (treeNode instanceof CrucibleFileNode) {
			CrucibleFileNode node = (CrucibleFileNode) treeNode;
            addCommentToFile(event, node.getReview(), node.getFile());
		}
	}

	private void addCommentToFile(AnActionEvent event, final ReviewAdapter review, final CrucibleFileInfo file) {
		final VersionedComment newComment = new VersionedComment(review.getReview(), file);
        newComment.setReviewItemId(review.getPermId());

        CommentTooltipPanel.showCommentTooltipPopup(
                event,
                new CommentTooltipPanelWithRunners(event, review, file, newComment, null, CommentTooltipPanel.Mode.ADD),
                null);
	}

	// pstefaniak: wseliga has told me something about obsolete routines regarding VersionedComment,
	// i suppose that must be one of those...
	private void addReplyToVersionedComment(final AnActionEvent event, final ReviewAdapter review,
                                            final CrucibleFileInfo file, final VersionedComment comment) {
		final VersionedComment newComment = new VersionedComment(review.getReview(), file);
        newComment.setReply(true);
        newComment.setFromLineInfo(comment.isFromLineInfo());
        newComment.setFromStartLine(comment.getFromStartLine());
        newComment.setFromEndLine(comment.getFromEndLine());
        newComment.setToLineInfo(comment.isToLineInfo());
        newComment.setToStartLine(comment.getToStartLine());
        newComment.setToEndLine(comment.getToEndLine());
        newComment.setLineRanges(comment.getLineRanges());
        newComment.setCreateDate(new Date());
        newComment.setReviewItemId(review.getPermId());
        newComment.setAuthor(new User(review.getServerData().getUsername()));

        CommentTooltipPanel.showCommentTooltipPopup(
                event,
                new CommentTooltipPanelWithRunners(event, review, file, newComment, comment, CommentTooltipPanel.Mode.ADD),
                null);
	}

	private void addReplyToGeneralComment(final AnActionEvent event, final ReviewAdapter review,
                                          final Comment parentComment) {

		final GeneralComment newComment = new GeneralComment(review.getReview(), parentComment);
		newComment.setReply(true);

       	newComment.setCreateDate(new Date());
		newComment.setAuthor(new User(review.getServerData().getUsername()));
        
        CommentTooltipPanel.showCommentTooltipPopup(
                event,
                new CommentTooltipPanelWithRunners(event, review, null, newComment,
                        parentComment, CommentTooltipPanel.Mode.ADD),
                null);
	}
}
