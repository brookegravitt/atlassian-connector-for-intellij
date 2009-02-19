package com.atlassian.theplugin.idea.action.crucible.comment.gutter;

import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import java.util.Date;

public class ReplyAction extends AbstractGutterCommentAction {
	public void actionPerformed(final AnActionEvent anActionEvent) {
		final Project project = anActionEvent.getData(DataKeys.PROJECT);
		final VersionedCommentBean newComment = new VersionedCommentBean();
		newComment.setReply(true);
		CommentEditForm dialog = new CommentEditForm(project, review, newComment,
				CrucibleHelper.getMetricsForReview(project, review));
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setFromLineInfo(comment.isFromLineInfo());
			newComment.setFromStartLine(comment.getFromStartLine());
			newComment.setFromEndLine(comment.getFromEndLine());
			newComment.setToLineInfo(comment.isToLineInfo());
			newComment.setToStartLine(comment.getToStartLine());
			newComment.setToEndLine(comment.getToEndLine());
			newComment.setCreateDate(new Date());
			newComment.setReviewItemId(review.getPermId());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));

			Task.Backgroundable task = new Task.Backgroundable(project, "Adding File Comment Reply", false) {

				public void run(final ProgressIndicator indicator) {
					try {
						review.addVersionedCommentReply(file, comment, newComment);
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
