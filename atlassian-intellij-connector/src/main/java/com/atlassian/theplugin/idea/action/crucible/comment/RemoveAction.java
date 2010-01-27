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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

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
		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
		e.getPresentation().setText(text);
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project currentProject = e.getData(DataKeys.PROJECT);
		AtlassianTreeNode node = getSelectedNode(e);
		if (node != null && currentProject != null) {
			removeComment(currentProject, node);
		}
	}


	private void removeComment(Project project, AtlassianTreeNode treeNode) {
		ReviewAdapter review = null;

		if (treeNode instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
			Comment comment = node.getComment();
			review = node.getReview();

			if (comment == null || review == null) {
				Messages.showErrorDialog(project, "Comment or review not found in the tree node", "Error");
				return;
			}

			if (RemoveCommentConfirmation.userAgreed(project)) {
				removeGeneralComment(project, review, comment);
			}
		} else if (treeNode instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
			VersionedComment comment = node.getComment();
			review = node.getReview();
			CrucibleFileInfo file = node.getFile();

			if (comment == null || review == null) {
				Messages.showErrorDialog(project, "Comment or review not found in the tree node", "Error");
				return;
			}

			if (RemoveCommentConfirmation.userAgreed(project)) {
				removeVersionedComment(project, review, comment, file);
			}
		}
	}

	private void removeVersionedComment(final Project project, final ReviewAdapter review, final VersionedComment comment,
			final CrucibleFileInfo file) {

//		review.removeVersionedComment(comment);

		Task.Backgroundable task = new Task.Backgroundable(project, "Removing File Comment", false) {

			@Override
			public void run(final ProgressIndicator indicator) {
				try {
					review.removeVersionedComment(comment, file);
				} catch (RemoteApiException e) {
					IdeaHelper.handleRemoteApiException(project, e);
				} catch (ServerPasswordNotProvidedException e) {
					IdeaHelper.handleMissingPassword(e);
				}
			}
		};

		ProgressManager.getInstance().run(task);
	}

	private void removeGeneralComment(final Project project, final ReviewAdapter review, final Comment comment) {

		Task.Backgroundable task = new Task.Backgroundable(project, "Removing General Comment", false) {

			@Override
			public void run(final ProgressIndicator indicator) {
				try {
					review.removeGeneralComment(comment);
				} catch (RemoteApiException e) {
					IdeaHelper.handleRemoteApiException(project, e);
				} catch (ServerPasswordNotProvidedException e) {
					IdeaHelper.handleMissingPassword(e);
				}
			}
		};

		ProgressManager.getInstance().run(task);
	}

}
