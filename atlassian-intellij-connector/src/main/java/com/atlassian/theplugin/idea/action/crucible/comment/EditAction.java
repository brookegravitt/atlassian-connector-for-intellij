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

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;


public class EditAction extends AbstractCommentAction {
	private static final String EDIT_TEXT = "Edit";

	@Override
	public void update(AnActionEvent e) {
		AtlassianTreeNode node = getSelectedNode(e);
		String text = EDIT_TEXT;
		boolean enabled = node != null && checkIfAuthor(node);
		e.getPresentation().setEnabled(enabled);
		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
		e.getPresentation().setText(text);
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
			editGeneralComment(project, node.getReview(), comment, null, null);
		} else if (treeNode instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
			VersionedComment comment = node.getComment();
			editVersionedComment(project, node.getReview(), node.getFile(), comment, null, null);
		}
	}

	private void editGeneralComment(final Project project, final ReviewAdapter review, final GeneralComment comment,
			GeneralCommentBean localCopy,
			final String errorMessage) {

		final GeneralCommentBean localData;
		if (localCopy == null) {
			localData = new GeneralCommentBean(comment);
		} else {
			localData = localCopy;
		}

		final CommentEditForm dialog = new CommentEditForm(project, review, localData, errorMessage);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

			Task.Backgroundable task = new Task.Backgroundable(project, "Editing General Comment", false) {

				public void run(@NotNull final ProgressIndicator indicator) {

					try {
						review.editGeneralComment(localData);
					} catch (final Exception e) {
						ApplicationManager.getApplication().invokeLater(new Runnable() {

							public void run() {
								editGeneralComment(project, review, comment, localData, e.getMessage());
							}
						});
					}
				}
			};

			ProgressManager.getInstance().run(task);
		}
	}

	// PL-25
	private void editVersionedComment(final Project project, final ReviewAdapter review,
			final CrucibleFileInfo file, final VersionedComment comment, VersionedCommentBean localCopy, String errorMessage) {

		final VersionedCommentBean localData;
		if (localCopy == null) {
			localData = new VersionedCommentBean(comment);
		} else {
			localData = localCopy;
		}

		CommentEditForm dialog = new CommentEditForm(project, review, localData, errorMessage);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

			Task.Backgroundable task = new Task.Backgroundable(project, "Editing File Comment", false) {

				public void run(@NotNull final ProgressIndicator indicator) {

					try {
						review.editVersionedComment(file, localData);
					} catch (final Exception e) {
						ApplicationManager.getApplication().invokeLater(new Runnable() {

							public void run() {
								editVersionedComment(project, review, file, comment, localData, e.getMessage());
							}
						});
					}
				}
			};

			ProgressManager.getInstance().run(task);
		}
	}
}