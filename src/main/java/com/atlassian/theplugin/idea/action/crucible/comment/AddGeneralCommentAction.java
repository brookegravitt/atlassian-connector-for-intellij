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
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleToolWindow;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import java.util.Date;

import org.jetbrains.annotations.NotNull;

public class AddGeneralCommentAction extends AbstractCommentAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project currentProject = e.getData(DataKeys.PROJECT);
		if (currentProject != null && getReview(e) != null) {
			addGeneralComment(currentProject, getReview(e));
		}
	}

	@Override
	public void update(AnActionEvent e) {
		boolean enabled = getReview(e) != null && checkIfAuthorized(getReview(e));

		e.getPresentation().setEnabled(enabled);
		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
	}

	private ReviewAdapter getReview(final AnActionEvent e) {
		CrucibleToolWindow crucibleDetailsWindow = IdeaHelper.getProjectComponent(e, CrucibleToolWindow.class);
		if (crucibleDetailsWindow != null) {
			return crucibleDetailsWindow.getReview();
		}

		return null;
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

	private void addGeneralComment(final Project project, final ReviewAdapter review) {
		final GeneralCommentBean newComment = new GeneralCommentBean();
		CommentEditForm dialog = new CommentEditForm(project, review, newComment,
				CrucibleHelper.getMetricsForReview(project, review));
		dialog.setTitle("Add General Comment");
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setCreateDate(new Date());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));

			Task.Backgroundable task = new Task.Backgroundable(project, "Adding General Comment", false) {

				@Override
				public void run(@NotNull final ProgressIndicator indicator) {
					try {
						review.addGeneralComment(newComment);
					} catch (ValueNotYetInitialized valueNotYetInitialized) {
						IdeaHelper.handleError(project, valueNotYetInitialized);
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
}