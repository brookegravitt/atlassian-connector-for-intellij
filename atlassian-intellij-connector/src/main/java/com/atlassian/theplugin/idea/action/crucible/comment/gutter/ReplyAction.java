package com.atlassian.theplugin.idea.action.crucible.comment.gutter;

import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class ReplyAction extends AbstractGutterCommentAction {
	public void actionPerformed(final AnActionEvent anActionEvent) {
		final Project project = anActionEvent.getData(DataKeys.PROJECT);
		createComment(project, null, null);
	}

	private void createComment(final Project project, final VersionedCommentBean localCopy, Throwable error) {
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
			newComment.setAuthor(new User(review.getServerData().getUserName()));
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
								createComment(project, newComment, e);
							}
						});
					}
				}
			};

			ProgressManager.getInstance().run(task);
		}
	}
}
