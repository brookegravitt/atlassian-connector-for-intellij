package com.atlassian.theplugin.idea.action.crucible.comment.gutter;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
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

public class EditAction extends AbstractGutterCommentAction {
	public void actionPerformed(final AnActionEvent anActionEvent) {
		final Project project = anActionEvent.getData(DataKeys.PROJECT);
		editComment(project, comment, null, null);
	}

	private void editComment(final Project project, final VersionedComment comment, final VersionedComment localCopy,
			final Throwable error) {

		final VersionedComment newComment;
		if (localCopy != null) {
			newComment = new VersionedComment(localCopy);
		} else {
			newComment = new VersionedComment(comment);
		}

		CommentEditForm dialog = new CommentEditForm(project, review, newComment, error);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

			Task.Backgroundable task = new Task.Backgroundable(project, "Editing File Comment", false) {

				public void run(@NotNull final ProgressIndicator indicator) {

					try {
						review.editVersionedComment(file, newComment);
					} catch (final Exception e) {
						ApplicationManager.getApplication().invokeLater(new Runnable() {

							public void run() {
								editComment(project, newComment, newComment, e);
							}
						});
					}
				}
			};

			ProgressManager.getInstance().run(task);
		}
	}
}
