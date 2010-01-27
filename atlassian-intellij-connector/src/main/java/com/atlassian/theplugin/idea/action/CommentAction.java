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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanel;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanelWithRunners;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import javax.swing.Icon;
import java.util.Date;

/**
 * User: lguminski
 * Date: Aug 1, 2008
 * Time: 11:37:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommentAction extends AnAction {

	public CommentAction() {
		super();
		setEnabledInModalContext(true);
	}

	public CommentAction(final String s) {
		super(s);
		setEnabledInModalContext(true);
	}

	public CommentAction(final String s, final String s1, final Icon icon) {
		super(s, s1, icon);
		setEnabledInModalContext(true);
	}

	@Override
	public void update(final AnActionEvent e) {
		boolean visible = true;
		Editor editor = e.getData(DataKeys.EDITOR);
		if (editor == null) {
			visible = false;
		} else {
			int start = editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1;
			int end = editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionEnd()) + 1;
			if (end < start || start <= 0 || end <= 0) {
				visible = false;
			} else {
				Document document = editor.getDocument();
				VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
				if (virtualFile != null) {

					ReviewAdapter review = virtualFile.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
					CrucibleFileInfo reviewItem = virtualFile.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);
					if (review == null || reviewItem == null) {
						visible = false;
					} else {
						final Project project = e.getData(DataKeys.PROJECT);
						CrucibleReviewListModel crucibleReviewListModel
								= IdeaHelper.getProjectComponent(project, CrucibleReviewListModel.class);
						if (crucibleReviewListModel == null
								|| !crucibleReviewListModel.getOpenInIdeReviews().contains(review)) {
							visible = false;
						} else {
							try {
								if (!review.getActions().contains(CrucibleAction.COMMENT)) {
									visible = false;
								}
							} catch (ValueNotYetInitialized valueNotYetInitialized) {
								visible = false;
							}
						}
					}
				}
			}
		}
		e.getPresentation().setVisible(visible);
		e.getPresentation().setEnabled(visible);
	}

    @Override
    public void beforeActionPerformedUpdate(AnActionEvent event) {
        Editor editor = event.getData(DataKeys.EDITOR);
        event.getPresentation().setEnabled(editor != null);
    }

    @Override
	public void actionPerformed(final AnActionEvent e) {
		Editor editor = e.getData(DataKeys.EDITOR);
		if (editor == null) {
			return;
		}

        e.getPresentation().putClientProperty(CommentTooltipPanel.JBPOPUP_PARENT_COMPONENT, editor.getComponent());

		final Document document = editor.getDocument();
		final VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
		if (virtualFile != null) {
			final int start = editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1;
			int selEndOffset = editor.getSelectionModel().getSelectionEnd();
			int end = editor.getDocument().getLineNumber(selEndOffset);
			int lastLineOffset = editor.getDocument().getLineStartOffset(end);

			// mind the fact that last line should not necessarily be included in the comment, because
			// the caret may be at the beginning of the line while selecting
			// start > end is to handle empty selections (just a caret set) at the very beginning of a line
			if (selEndOffset != lastLineOffset || start > end) {
				++end;
			}

			final ReviewAdapter review = virtualFile.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
			final CrucibleFileInfo file = virtualFile.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);

			// PL-833 - review can not be null when adding comment
			// doesn't look like action called by Idea but anyway should not be null
			if (review == null) {
				return;
			}

			createLineComment(e, review, file, start, end);
		}
	}

	private void createLineComment(AnActionEvent event, final ReviewAdapter review,
                                   final CrucibleFileInfo file, final int start, final int end) {

		final VersionedComment newComment = new VersionedComment(review.getReview(), file);
        newComment.setCreateDate(new Date());
        newComment.setAuthor(new User(review.getServerData().getUsername()));
        if (file.getCommitType() == CommitType.Deleted) {
            newComment.setFromStartLine(start);
            newComment.setFromEndLine(end);
            newComment.setFromLineInfo(true);
        } else {
            newComment.setToStartLine(start);
            newComment.setToEndLine(end);
            newComment.setToLineInfo(true);
        }

        CommentTooltipPanel.showCommentTooltipPopup(
                event,
                new CommentTooltipPanelWithRunners(event, review, file, newComment, null, CommentTooltipPanel.Mode.ADD),
                null);
	}
}
