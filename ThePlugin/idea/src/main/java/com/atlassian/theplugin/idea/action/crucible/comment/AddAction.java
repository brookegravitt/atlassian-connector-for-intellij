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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.CommentTreePanel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentReplyAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentReplyAboutToAdd;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleGeneralCommentsNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.idea.ui.tree.comment.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import java.util.Date;

public class AddAction extends AbstractCommentAction {
	private static final String REPLY_TEXT = "Reply";
	private static final String COMMENT_TEXT = "Add Comment";
	private static final String FILE_COMMENT_TEXT = "Add Revision Comment";
	private static final String GENERAL_COMMENT_TEXT = "Add General Comment";

	@Override
	public void update(AnActionEvent e) {
		AtlassianTreeNode node = getSelectedNode(e);
		String text = COMMENT_TEXT;
		boolean enabled = node != null && checkIfAuthorized(getReview(node));
		if (enabled) {
			if (node instanceof CrucibleFileNode) {
				text = FILE_COMMENT_TEXT;
			} else if (node instanceof CrucibleGeneralCommentsNode) {
				text = GENERAL_COMMENT_TEXT;
			}
			if (node instanceof VersionedCommentTreeNode) {
				final VersionedCommentTreeNode vcNode = (VersionedCommentTreeNode) node;
				if (vcNode.getComment().isReply()) {
					enabled = false;
				} else {
					text = REPLY_TEXT;
				}
			} else if (node instanceof GeneralCommentTreeNode) {
				final GeneralCommentTreeNode gcNode = (GeneralCommentTreeNode) node;
				if (gcNode.getComment().isReply()) {
					enabled = false;
				} else {
					text = REPLY_TEXT;
				}
			}
		}
		e.getPresentation().setEnabled(enabled);
		if (e.getPlace().equals(CommentTreePanel.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
		e.getPresentation().setText(text);
	}

	private boolean checkIfAuthorized(final ReviewData review) {
		if (review == null) {
			return false;
		}
		try {
			if (!review.getActions().contains(Action.COMMENT)) {
				return false;
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			return false;
		}
		return true;

	}

	private ReviewData getReview(final AtlassianTreeNode node) {
		if (node instanceof CommentTreeNode) {
			final CommentTreeNode cNode = (CommentTreeNode) node;
			return cNode.getReview();
		} else if (node instanceof GeneralSectionNode) {
			return ((GeneralSectionNode) node).getReview();
		} else if (node instanceof FileNameNode) {
			return ((FileNameNode) node).getReview();
		} else if (node instanceof CrucibleGeneralCommentsNode) {
			return ((CrucibleGeneralCommentsNode) node).getReview();
		} else if (node instanceof CrucibleFileNode) {
			return ((CrucibleFileNode) node).getReview();
		}
		return null;
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project currentProject = e.getData(DataKeys.PROJECT);
		com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode node = getSelectedNode(e);
		if (node != null && currentProject != null) {
			addComment(currentProject, node);
		}

	}

	private void addComment(Project project, AtlassianTreeNode treeNode) {
		if (treeNode instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
			addReplyToGeneralComment(project, node.getReview(), node.getComment());
		} else if (treeNode instanceof GeneralSectionNode) {
			GeneralSectionNode node = (GeneralSectionNode) treeNode;
			addGeneralComment(project, node.getReview());
		} else if (treeNode instanceof FileNameNode) {
			FileNameNode node = (FileNameNode) treeNode;
			addCommentToFile(project, node.getReview(), node.getFile());
		} else if (treeNode instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
			addReplyToVersionedComment(project, node.getReview(), node.getFile(), node.getComment());
		} else if (treeNode instanceof CrucibleGeneralCommentsNode) {
			CrucibleGeneralCommentsNode node = (CrucibleGeneralCommentsNode) treeNode;
			addGeneralComment(project, node.getReview());
		} else if (treeNode instanceof CrucibleFileNode) {
			CrucibleFileNode node = (CrucibleFileNode) treeNode;
			addCommentToFile(project, node.getReview(), node.getFile());
		}
	}

	private void addCommentToFile(Project project, ReviewData review, CrucibleFileInfo file) {
		VersionedCommentBean newComment = new VersionedCommentBean();
		CommentEditForm dialog = new CommentEditForm(project, review, (CommentBean) newComment,
				CrucibleHelper.getMetricsForReview(project, review));
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setCreateDate(new Date());
			newComment.setReviewItemId(review.getPermId());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));
			// @todo
			IdeaHelper.getReviewActionEventBroker(project).trigger(
					new VersionedCommentAboutToAdd(CrucibleReviewActionListener.ANONYMOUS,
							review, file, newComment));
		}

	}

	private void addReplyToVersionedComment(Project project, ReviewData review,
			CrucibleFileInfo file, VersionedComment comment) {
		VersionedCommentBean newComment = new VersionedCommentBean();
		newComment.setReply(true);
		CommentEditForm dialog = new CommentEditForm(project, review, (CommentBean) newComment,
				CrucibleHelper.getMetricsForReview(project, review));
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			VersionedComment parentComment = comment;
			newComment.setFromLineInfo(parentComment.isFromLineInfo());
			newComment.setFromStartLine(parentComment.getFromStartLine());
			newComment.setFromEndLine(parentComment.getFromEndLine());
			newComment.setToLineInfo(parentComment.isToLineInfo());
			newComment.setToStartLine(parentComment.getToStartLine());
			newComment.setToEndLine(parentComment.getToEndLine());
			newComment.setCreateDate(new Date());
			newComment.setReviewItemId(review.getPermId());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));
			IdeaHelper.getReviewActionEventBroker(project).trigger(
					new VersionedCommentReplyAboutToAdd(CrucibleReviewActionListener.ANONYMOUS,
							review, file, parentComment, newComment));
		}
	}

	private void addGeneralComment(Project project, ReviewData review) {
		GeneralCommentBean newComment = new GeneralCommentBean();
		CommentEditForm dialog = new CommentEditForm(project, review, newComment,
				CrucibleHelper.getMetricsForReview(project, review));
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setCreateDate(new Date());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));
			IdeaHelper.getReviewActionEventBroker(project).trigger(
					new GeneralCommentAboutToAdd(CrucibleReviewActionListener.ANONYMOUS,
							review, newComment));
		}
	}

	private void addReplyToGeneralComment(Project project, ReviewData review, GeneralComment comment) {
		GeneralComment parentComment = comment;
		GeneralCommentBean newComment = new GeneralCommentBean();
		newComment.setReply(true);
		CommentEditForm dialog = new CommentEditForm(project, review, newComment,
				CrucibleHelper.getMetricsForReview(project, review));
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setCreateDate(new Date());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));
			IdeaHelper.getReviewActionEventBroker(project).trigger(
					new GeneralCommentReplyAboutToAdd(CrucibleReviewActionListener.ANONYMOUS,
							review, parentComment, newComment));
		}
	}


}
