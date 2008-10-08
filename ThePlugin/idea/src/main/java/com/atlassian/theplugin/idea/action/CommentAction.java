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
import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.AddLineComment;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

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
				Review review = ed.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
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
		Project project = DataKeys.PROJECT.getData(e.getDataContext());

		int start = ed.getDocument().getLineNumber(ed.getSelectionModel().getSelectionStart());
		int selEndOffset = ed.getSelectionModel().getSelectionEnd();
		int end = ed.getDocument().getLineNumber(selEndOffset);
		int lastLineOffset = ed.getDocument().getLineStartOffset(end);

		++start;
		// mind the fact that last line should not necessarily be included in the comment, because
		// the caret may be at the beginning of the line while selecting
		if (selEndOffset != lastLineOffset) {
			++end;
		}

		Review review = ed.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
		CrucibleFileInfo reviewItem = ed.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);

		AddLineComment addComment = new AddLineComment(CrucibleReviewActionListener.ANONYMOUS,
				review, reviewItem, ed, start, end);
		IdeaHelper.getReviewActionEventBroker(project).trigger(addComment);
	}
}
