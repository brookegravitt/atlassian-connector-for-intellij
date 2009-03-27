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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.FileNameNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralSectionNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
			} else if (node instanceof VersionedCommentTreeNode) {
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
		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
		e.getPresentation().setText(text);
	}

	private boolean checkIfAuthorized(final ReviewAdapter review) {
		if (review == null) {
			return false;
		}
		try {
			if (!review.getActions().contains(CrucibleAction.COMMENT)) {
				return false;
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
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
		Project currentProject = e.getData(DataKeys.PROJECT);
		com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode node = getSelectedNode(e);
		if (node != null && currentProject != null) {
			addComment(currentProject, node);
		}
	}

	private void addComment(Project project, AtlassianTreeNode treeNode) {
		if (treeNode instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
			addReplyToGeneralComment(project, node.getReview(), node.getComment(), null, null);
		} else if (treeNode instanceof FileNameNode) {
			FileNameNode node = (FileNameNode) treeNode;
			addCommentToFile(project, node.getReview(), node.getFile(), null, null);
		} else if (treeNode instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
			addReplyToVersionedComment(project, node.getReview(), node.getFile(), node.getComment(), null, null);
		} else if (treeNode instanceof CrucibleFileNode) {
			CrucibleFileNode node = (CrucibleFileNode) treeNode;
			addCommentToFile(project, node.getReview(), node.getFile(), null, null);
		}
	}

	private void addCommentToFile(final Project project, final ReviewAdapter review, final CrucibleFileInfo file,
			final VersionedCommentBean localCopy, final Throwable error) {
		final VersionedCommentBean newComment;
		if (localCopy != null) {
			newComment = new VersionedCommentBean(localCopy);
		} else {
			newComment = new VersionedCommentBean();
			newComment.setCreateDate(new Date());
			newComment.setReviewItemId(review.getPermId());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));
		}
		CommentEditForm dialog = new CommentEditForm(project, review, newComment, error);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

			Task.Backgroundable task = new Task.Backgroundable(project, "Adding File Comment", false) {

				public void run(@NotNull final ProgressIndicator indicator) {

					try {
						review.addVersionedComment(file, newComment);
					} catch (final Exception e) {
						ApplicationManager.getApplication().invokeLater(new Runnable() {

							public void run() {
								addCommentToFile(project, review, file, newComment, e);
							}
						});
					}
				}
			};

			ProgressManager.getInstance().run(task);
		}
	}

	private void addReplyToVersionedComment(final Project project, final ReviewAdapter review,
			final CrucibleFileInfo file, final VersionedComment comment, final VersionedCommentBean localCopy,
			final Throwable error) {
		final VersionedCommentBean newComment;
		if (localCopy != null) {
			newComment = new VersionedCommentBean(localCopy);
		} else {
			newComment = new VersionedCommentBean();
			newComment.setReply(true);
			newComment.setFromLineInfo(comment.isFromLineInfo());
			newComment.setFromStartLine(comment.getFromStartLine());
			newComment.setFromEndLine(comment.getFromEndLine());
			newComment.setToLineInfo(comment.isToLineInfo());
			newComment.setToStartLine(comment.getToStartLine());
			newComment.setToEndLine(comment.getToEndLine());
			newComment.setCreateDate(new Date());
			newComment.setReviewItemId(review.getPermId());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));
		}

		CommentEditForm dialog = new CommentEditForm(project, review, newComment, error);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

			Task.Backgroundable task = new Task.Backgroundable(project, "Adding File Comment Reply", false) {

				public void run(@NotNull final ProgressIndicator indicator) {
					try {
						review.addVersionedCommentReply(file, comment, newComment);
					} catch (final Exception e) {
						ApplicationManager.getApplication().invokeLater(new Runnable() {

							public void run() {
								addReplyToVersionedComment(project, review, file, comment, newComment, e);
							}
						});

					}
				}
			};

			ProgressManager.getInstance().run(task);
		}
	}

	private void addReplyToGeneralComment(final Project project, final ReviewAdapter review,
			final GeneralComment parentComment, final GeneralCommentBean localCopy, final Throwable error) {
		final GeneralCommentBean newComment;
		if (localCopy != null) {
			newComment = new GeneralCommentBean(localCopy);
		} else {
			newComment = new GeneralCommentBean();
			newComment.setReply(true);
		}

		CommentEditForm dialog = new CommentEditForm(project, review, newComment, error);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setCreateDate(new Date());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));

			Task.Backgroundable task = new Task.Backgroundable(project, "Adding General Comment Reply", false) {

				public void run(@NotNull final ProgressIndicator indicator) {

					try {
						review.addGeneralCommentReply(parentComment, newComment);
					} catch (final Exception e) {
						ApplicationManager.getApplication().invokeLater(new Runnable() {

							public void run() {
								addReplyToGeneralComment(project, review, parentComment, newComment, e);
							}
						});
					}
				}
			};

			ProgressManager.getInstance().run(task);
		}
	}
}
