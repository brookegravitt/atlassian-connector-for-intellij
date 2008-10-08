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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

import java.awt.*;

public final class CommentHighlighter {
	private static final Color VERSIONED_COMMENT_BACKGROUND_COLOR = new Color(255, 219, 90);
	private static final Color VERSIONED_COMMENT_STRIP_MARK_COLOR = VERSIONED_COMMENT_BACKGROUND_COLOR;

	private static final String REVIEW_DATA_KEY_NAME = "REVIEW_DATA_KEY";
	public static final Key<Review> REVIEW_DATA_KEY = Key.create(REVIEW_DATA_KEY_NAME);

	private static final String REVIEWITEM_DATA_KEY_NAME = "REVIEW_ITEM_DATA_KEY";
	public static final Key<CrucibleFileInfo> REVIEWITEM_DATA_KEY = Key.create(REVIEWITEM_DATA_KEY_NAME);

	private static final String COMMENT_DATA_KEY_NAME = "CRUCIBLE_COMMENT_DATA_KEY";
	private static final Key<Boolean> COMMENT_DATA_KEY = Key.create(COMMENT_DATA_KEY_NAME);
	private static final Key<DocumentListener> LISTENER_KEY = Key.create("CRUCIBLE_COMMENT_DOCUMENT_LISTENER");


	private CommentHighlighter() {
	}

	public static void highlightCommentsInEditor(final Project project, final Editor editor, Review review,
			CrucibleFileInfo reviewItem) {
		if (editor != null) {

			applyHighlighters(project, editor, reviewItem);
			editor.putUserData(REVIEW_DATA_KEY, review);
			editor.putUserData(REVIEWITEM_DATA_KEY, reviewItem);
			editor.putUserData(COMMENT_DATA_KEY, true);
			DocumentListener documentListener = editor.getUserData(LISTENER_KEY);
			if (documentListener == null) {
				documentListener = new DocumentListener() {
					public void beforeDocumentChange(final DocumentEvent event) {
					}

					public void documentChanged(final DocumentEvent event) {
						ApplicationManager.getApplication().invokeLater(new Runnable() {
							public void run() {
								removeHighlighters(editor.getDocument().getMarkupModel(project));
							}
						});
					}
				};
				editor.getDocument().addDocumentListener(documentListener);
				editor.putUserData(LISTENER_KEY, documentListener);
			}

		}
	}

	public static void updateCommentsInEditors(Project project, Review review, CrucibleFileInfo reviewItem) {
		for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
			if (editor.getUserData(COMMENT_DATA_KEY) != null) {
				final Review data = editor.getUserData(REVIEW_DATA_KEY);
				final CrucibleFileInfo file = editor.getUserData(REVIEWITEM_DATA_KEY);
				if (data != null && file != null) {
					if (data.getPermId().equals(review.getPermId())
							&& file.getItemInfo().getId().equals(reviewItem.getItemInfo().getId())) {
						applyHighlighters(project, editor, reviewItem);
						editor.putUserData(REVIEW_DATA_KEY, review);
						editor.putUserData(REVIEWITEM_DATA_KEY, reviewItem);
						editor.putUserData(COMMENT_DATA_KEY, true);
					}
				}
			}
		}
	}

	private static void applyHighlighters(final Project project, final Editor editor, final CrucibleFileInfo reviewItem) {
		final MarkupModel markupModel = editor.getDocument().getMarkupModel(project);
		removeHighlighters(markupModel);

		TextAttributes textAttributes = new TextAttributes();
		textAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);
		for (VersionedComment comment : reviewItem.getItemInfo().getComments()) {
			if (comment.getToStartLine() > 0) {
				int endLine =  comment.getToEndLine() > 0 ? comment.getToEndLine() : comment.getToStartLine();
				try {
					final int startOffset = editor.getDocument().getLineStartOffset(comment.getToStartLine() - 1);
					final int endOffset = editor.getDocument().getLineEndOffset(endLine - 1);
					RangeHighlighter rh = markupModel.addRangeHighlighter(startOffset, endOffset - 1,
							HighlighterLayer.WARNING - 1, textAttributes, HighlighterTargetArea.LINES_IN_RANGE);
						rh.setErrorStripeTooltip("<html><b>" + comment.getAuthor().getDisplayName()
								+ ":</b> " + comment.getMessage());
						rh.setErrorStripeMarkColor(VERSIONED_COMMENT_STRIP_MARK_COLOR);
						rh.putUserData(COMMENT_DATA_KEY, true);
				} catch (Exception e) {
					PluginUtil.getLogger().error(e);
				}
			}
		}
	}

	private static void removeHighlighters(final MarkupModel markupModel) {
		for (RangeHighlighter rh : markupModel.getAllHighlighters()) {
			if (rh.getUserData(COMMENT_DATA_KEY) != null) {
				markupModel.removeHighlighter(rh);
			}
		}
	}
}