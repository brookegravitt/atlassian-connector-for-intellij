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

package com.atlassian.theplugin.idea.action;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import java.awt.*;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Aug 1, 2008
 * Time: 11:37:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommentAction extends AnAction {

	@Override
	public void update(final AnActionEvent e) {
		boolean visible = true;
		Editor ed = e.getData(DataKeys.EDITOR);
		if (ed == null) {
			visible = false;
		} else {
			int start = ed.getDocument().getLineNumber(ed.getSelectionModel().getSelectionStart()) + 1;
			int end = ed.getDocument().getLineNumber(ed.getSelectionModel().getSelectionEnd()) + 1;
			if (end < start || start <= 0 || end <= 0) {
				visible = false;
			} else {
				ReviewAdapter review = ed.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
				CrucibleFileInfo reviewItem = ed.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);
				if (review == null || reviewItem == null) {
					visible = false;
				} else {
					try {
						if (!review.getActions().contains(Action.COMMENT)) {
							visible = false;
						}
					} catch (ValueNotYetInitialized valueNotYetInitialized) {
						visible = false;
					}
				}
			}
		}
		e.getPresentation().setVisible(visible);
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		Editor ed = e.getData(DataKeys.EDITOR);
		if (ed == null) {
			return;
		}
		final Project project = e.getData(DataKeys.PROJECT);
		if (project == null) {
			return;
		}

		final int start = ed.getDocument().getLineNumber(ed.getSelectionModel().getSelectionStart()) + 1;
		int selEndOffset = ed.getSelectionModel().getSelectionEnd();
		int end = ed.getDocument().getLineNumber(selEndOffset);
		int lastLineOffset = ed.getDocument().getLineStartOffset(end);

		// mind the fact that last line should not necessarily be included in the comment, because
		// the caret may be at the beginning of the line while selecting
		// start > end is to handle empty selections (just a caret set) at the very beginning of a line
		if (selEndOffset != lastLineOffset || start > end) {
			++end;
		}

		final ReviewAdapter review = ed.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
		final CrucibleFileInfo file = ed.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);


		final VersionedCommentBean newComment = new VersionedCommentBean();
		CommentEditForm dialog = new CommentEditForm(project, review, newComment,
				CrucibleHelper.getMetricsForReview(project, review));
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setCreateDate(new Date());
			newComment.setAuthor(new UserBean(review.getServer().getUsername()));
			newComment.setToStartLine(start);
			newComment.setToEndLine(end);

			Task.Backgroundable task = new Task.Backgroundable(project, "Adding line comment", false) {
				public void run(final ProgressIndicator indicator) {
					try {
						review.addVersionedComment(file, newComment);
					} catch (RemoteApiException e1) {
						IdeaHelper.handleRemoteApiException(project, e1);
					} catch (ServerPasswordNotProvidedException e1) {
						IdeaHelper.handleMissingPassword(e1);
					}

//			eventBroker.trigger(new VersionedCommentAddedOrEdited(this, review, file.getPermId(), newComment));

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							Editor editor = CrucibleHelper.getEditorForCrucibleFile(review, file);
							if (editor != null) {
								CommentHighlighter.highlightCommentsInEditor(project, editor, review, file);
							}
						}
					});

				}
			};

			ProgressManager.getInstance().run(task);
		}


//		AddLineComment addComment = new AddLineComment(CrucibleReviewListenerImpl.ANONYMOUS,
//				review, file, ed, start, end);
//		IdeaHelper.getReviewActionEventBroker(project).trigger(addComment);
	}
}
