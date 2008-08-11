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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

import java.awt.*;

public final class CommentHighlighter {
	private static final Color VERSIONED_COMMENT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
	private static final Color VERSIONED_COMMENT_STRIP_MARK_COLOR = Color.ORANGE;

	public static final String REVIEW_DATA_KEY_NAME = "REVIEW_DATA_KEY";
	public static final Key<ReviewData> REVIEW_DATA_KEY = Key.create(REVIEW_DATA_KEY_NAME);

	public static final String REVIEWITEM_DATA_KEY_NAME = "REVIEW_ITEM_DATA_KEY";
	public static final Key<CrucibleFileInfo> REVIEWITEM_DATA_KEY = Key.create(REVIEWITEM_DATA_KEY_NAME);

	public static final String COMMENT_DATA_KEY_NAME = "CRUCIBLE_COMMENT_DATA_KEY";
	public static final Key<Boolean> COMMENT_DATA_KEY = Key.create(COMMENT_DATA_KEY_NAME);


	private CommentHighlighter() {
	}

	public static void highlightCommentsInEditor(Project project, Editor editor, ReviewData review,
			CrucibleFileInfo reviewItem) {
		if (editor != null) {

			applyHighlighters(project, editor, reviewItem);
			editor.putUserData(REVIEW_DATA_KEY, review);
			editor.putUserData(REVIEWITEM_DATA_KEY, reviewItem);
			editor.putUserData(COMMENT_DATA_KEY, Boolean.valueOf(true));
		}
	}

	public static void updateCommentsInEditors(Project project, ReviewData review, CrucibleFileInfo reviewItem) {
		for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
			if (editor.getUserData(COMMENT_DATA_KEY) != null) {
				final ReviewData data = editor.getUserData(REVIEW_DATA_KEY);
				final CrucibleFileInfo file = editor.getUserData(REVIEWITEM_DATA_KEY);
				if (data != null && file != null) {
					if (data.getPermId().equals(review.getPermId())
							&& file.getPermId().equals(reviewItem.getPermId())) {
						applyHighlighters(project, editor, reviewItem);
						editor.putUserData(REVIEW_DATA_KEY, review);
						editor.putUserData(REVIEWITEM_DATA_KEY, reviewItem);
						editor.putUserData(COMMENT_DATA_KEY, Boolean.valueOf(true));
					}
				}
			}
		}
	}

	private static void applyHighlighters(final Project project, final Editor editor, final CrucibleFileInfo reviewItem) {
		final MarkupModel markupModel = editor.getDocument().getMarkupModel(project);
		for (RangeHighlighter rh : markupModel.getAllHighlighters()) {
			if (rh.getUserData(COMMENT_DATA_KEY) != null) {
				markupModel.removeHighlighter(rh);
			}
		}

		TextAttributes textAttributes = new TextAttributes();
		textAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);
		try {
			for (VersionedComment comment : reviewItem.getVersionedComments()) {
				if (comment.getToStartLine() > 0 && comment.getToEndLine() > 0) {
					try {
						for (int i = comment.getToStartLine() - 1; i < comment.getToEndLine(); i++) {
							RangeHighlighter rh = markupModel.addLineHighlighter(
									i, HighlighterLayer.SELECTION, textAttributes);
							rh.setErrorStripeTooltip(comment.getAuthor().getDisplayName() + ":" + comment.getMessage());
							rh.setErrorStripeMarkColor(VERSIONED_COMMENT_STRIP_MARK_COLOR);
							rh.putUserData(COMMENT_DATA_KEY, Boolean.valueOf(true));
						}
					} catch (Exception e) {
						PluginUtil.getLogger().error(e);
					}
				}
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			PluginUtil.getLogger().error(valueNotYetInitialized);
		}
	}
}