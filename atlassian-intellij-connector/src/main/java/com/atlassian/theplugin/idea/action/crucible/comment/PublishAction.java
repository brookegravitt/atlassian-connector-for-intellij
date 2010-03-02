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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: Aug 5, 2008
 * Time: 3:03:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublishAction extends AbstractCommentAction {
	@Override
	public void update(AnActionEvent e) {
		AtlassianTreeNode node = getSelectedNode(e);
		boolean enabled = node != null && checkIfDraftAndAuthor(node) && isPublishable(node);
		e.getPresentation().setEnabled(enabled);
		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
	}

	protected boolean isPublishable(@NotNull final AtlassianTreeNode node) {
		if (node instanceof CommentTreeNode) {
			CommentTreeNode anode = (CommentTreeNode) node;
			Comment comment = anode.getComment();
			return !(comment.getParentComment() != null && comment.getParentComment().isDraft());
		}
		return false;
	}

	public void actionPerformed(AnActionEvent e) {
		Project currentProject = e.getData(DataKeys.PROJECT);
		AtlassianTreeNode node = getSelectedNode(e);
		if (node != null && currentProject != null) {
			publishComment(currentProject, node);
		}
	}

	private void publishComment(final Project project, AtlassianTreeNode treeNode) {
		if (treeNode instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
			Comment comment = node.getComment();

			publishGeneralCommen(project, node.getReview(), comment);

		} else if (treeNode instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
			VersionedComment comment = node.getComment();

			publishVersionedComment(project, node.getFile(), node.getReview(), comment);

		}
	}


	private void publishVersionedComment(final Project project, final CrucibleFileInfo file, final ReviewAdapter review,
			final VersionedComment comment) {

		Task.Backgroundable task = new Task.Backgroundable(project, "Publishing File Comment", false) {

			public void run(final ProgressIndicator indicator) {
				try {
					review.publishVersionedComment(file, comment);
				} catch (RemoteApiException e) {
					IdeaHelper.handleRemoteApiException(project, e);
				} catch (ServerPasswordNotProvidedException e) {
					IdeaHelper.handleMissingPassword(e);
				}
			}
		};
		ProgressManager.getInstance().run(task);
	}


	private void publishGeneralCommen(final Project project, final ReviewAdapter review, final Comment comment) {

		Task.Backgroundable task = new Task.Backgroundable(project, "Publishing General Comment", false) {

			public void run(final ProgressIndicator indicator) {
				try {
					review.publishGeneralComment(comment);
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
