package com.atlassian.theplugin.idea.action;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.AddLineComment;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;

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
		}
		String text = ed.getSelectionModel().getSelectedText();
		if (StringUtil.isEmptyOrSpaces(text)) {
			visible = false;
		}
		ReviewData review = ed.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
		CrucibleFileInfo reviewItem = ed.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);
		if (review == null || reviewItem == null) {
			visible = false;
		}
		e.getPresentation().setVisible(visible);
	}

	public void actionPerformed(final AnActionEvent e) {
		Editor ed = e.getData(DataKeys.EDITOR);
		if (ed == null) {
			return;
		}

		int start = ed.getDocument().getLineNumber(ed.getSelectionModel().getSelectionStart()) + 1;
		int end = ed.getDocument().getLineNumber(ed.getSelectionModel().getSelectionEnd()) + 1;

		ReviewData review = ed.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
		CrucibleFileInfo reviewItem = ed.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);

		AddLineComment addComment = new AddLineComment(CrucibleReviewActionListener.ANONYMOUS,
				review, reviewItem, ed, start, end);
		IdeaHelper.getReviewActionEventBroker().trigger(addComment);
	}
}
