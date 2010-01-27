package com.atlassian.theplugin.idea.action.crucible.comment.gutter;

import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.crucible.comment.RemoveCommentConfirmation;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

public class RemoveAction extends AbstractGutterCommentAction {
	@Override
	public void actionPerformed(final AnActionEvent anActionEvent) {
		final Project project = anActionEvent.getData(DataKeys.PROJECT);
		if (RemoveCommentConfirmation.userAgreed(project)) {
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
	}
}
