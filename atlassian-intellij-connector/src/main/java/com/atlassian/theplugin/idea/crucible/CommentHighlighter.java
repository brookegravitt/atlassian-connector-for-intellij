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
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Set;

public final class CommentHighlighter {
	private static final Color VERSIONED_COMMENT_BACKGROUND_COLOR = new Color(255, 219, 90);
	private static final Color VERSIONED_COMMENT_STRIP_MARK_COLOR = VERSIONED_COMMENT_BACKGROUND_COLOR;

	private static final String REVIEW_DATA_KEY_NAME = "REVIEW_DATA_KEY";
	public static final Key<ReviewAdapter> REVIEW_DATA_KEY = Key.create(REVIEW_DATA_KEY_NAME);

	private static final String REVIEWITEM_DATA_KEY_NAME = "REVIEW_ITEM_DATA_KEY";
	public static final Key<CrucibleFileInfo> REVIEWITEM_DATA_KEY = Key.create(REVIEWITEM_DATA_KEY_NAME);

	private static final String COMMENT_DATA_KEY_NAME = "CRUCIBLE_COMMENT_DATA_KEY";
	private static final Key<Boolean> COMMENT_DATA_KEY = Key.create(COMMENT_DATA_KEY_NAME);

	private static final String VERSIONED_COMMENT_DATA_KEY_NAME = "CRUCIBLE_COMMENT_DATA_KEY";
	public static final Key<VersionedComment> VERSIONED_COMMENT_DATA_KEY = Key.create(VERSIONED_COMMENT_DATA_KEY_NAME);

	private static final Key<DocumentListener> LISTENER_KEY = Key.create("CRUCIBLE_COMMENT_DOCUMENT_LISTENER");


	private CommentHighlighter() {
	}

	public static void highlightCommentsInEditor(@NotNull final Project project,
			@NotNull final Editor editor,
			@NotNull final ReviewAdapter review,
			@NotNull final CrucibleFileInfo reviewItem,
			@Nullable final OpenFileDescriptor displayFile) {
		if (editor != null) {
			applyHighlighters(project, editor, reviewItem);
			Document doc = editor.getDocument();
			VirtualFile vf = FileDocumentManager.getInstance().getFile(doc);
			vf.putUserData(REVIEW_DATA_KEY, review);
			vf.putUserData(REVIEWITEM_DATA_KEY, reviewItem);
			vf.putUserData(COMMENT_DATA_KEY, true);
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
				doc.addDocumentListener(documentListener);
				vf.putUserData(LISTENER_KEY, documentListener);
			}
			if (displayFile != null) {
				if (displayFile.canNavigateToSource()) {
					displayFile.navigateIn(editor);
				}
			}

		}
	}


	public static void updateCommentsInEditors(@NotNull final Project project,
			@NotNull final ReviewAdapter review) {
		for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
			Document document = editor.getDocument();
			VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
			if (virtualFile.getUserData(COMMENT_DATA_KEY) != null) {
				final ReviewAdapter data = virtualFile.getUserData(REVIEW_DATA_KEY);
				final CrucibleFileInfo file = virtualFile.getUserData(REVIEWITEM_DATA_KEY);
				if (data != null && file != null) {
					if (data.getPermId().equals(review.getPermId())) {
						try {
							for (CrucibleFileInfo crucibleFileInfo : data.getFiles()) {
								if (crucibleFileInfo.equals(file)) {
									applyHighlighters(project, editor, crucibleFileInfo);
									virtualFile.putUserData(REVIEW_DATA_KEY, review);
									virtualFile.putUserData(REVIEWITEM_DATA_KEY, crucibleFileInfo);
									virtualFile.putUserData(COMMENT_DATA_KEY, true);
								}
							}
						} catch (ValueNotYetInitialized valueNotYetInitialized) {
							throw new RuntimeException(valueNotYetInitialized);
						}
					} else {
						removeHighlightersAndContextData(editor.getDocument().getMarkupModel(project), virtualFile);
					}
				}
			} else {
				try {
					Set<CrucibleFileInfo> files = review.getFiles();
					for (CrucibleFileInfo fileInfo : files) {
						PsiFile f = CodeNavigationUtil
								.guessCorrespondingPsiFile(project, fileInfo.getFileDescriptor().getName());
						if (f != null) {
							VirtualFile virtualFile2 = FileDocumentManager.getInstance().getFile(document);
							if (virtualFile2.equals(f.getVirtualFile())) {
								applyHighlighters(project, editor, fileInfo);
								virtualFile.putUserData(REVIEW_DATA_KEY, review);
								virtualFile.putUserData(REVIEWITEM_DATA_KEY, fileInfo);
								virtualFile.putUserData(COMMENT_DATA_KEY, true);
							}
							break;
						}
					}
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
					// don't do anything - should not happen, but even if happens - we don't want to break file opening
				}
			}
		}
	}

	private static void applyHighlighters(@NotNull final Project project,
			@NotNull final Editor editor,
			@NotNull final CrucibleFileInfo fileInfo) {
		final MarkupModel markupModel = editor.getDocument().getMarkupModel(project);
		removeHighlighters(markupModel);

		TextAttributes textAttributes = new TextAttributes();
		textAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);
		for (VersionedComment comment : fileInfo.getVersionedComments()) {
			if (comment.getToStartLine() > 0) {
				int endLine = comment.getToEndLine() > 0 ? comment.getToEndLine() : comment.getToStartLine();
				try {
					final int startOffset = editor.getDocument().getLineStartOffset(comment.getToStartLine() - 1);
					int endOffset = editor.getDocument().getLineEndOffset(endLine - 1);
					if (startOffset < endOffset) {
						endOffset--;
					}
					RangeHighlighter rh = markupModel.addRangeHighlighter(startOffset, endOffset,
							HighlighterLayer.SELECTION - 1, textAttributes, HighlighterTargetArea.LINES_IN_RANGE);
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

	private static void removeHighlighters(@NotNull final MarkupModel markupModel) {
		for (RangeHighlighter rh : markupModel.getAllHighlighters()) {
			if (rh.getUserData(COMMENT_DATA_KEY) != null) {
				markupModel.removeHighlighter(rh);
			}
		}
	}

	private static void removeHighlightersAndContextData(@NotNull final MarkupModel markupModel,
			@NotNull VirtualFile virtualFile) {
		removeHighlighters(markupModel);
		virtualFile.putUserData(REVIEW_DATA_KEY, null);
		virtualFile.putUserData(REVIEWITEM_DATA_KEY, null);
		virtualFile.putUserData(COMMENT_DATA_KEY, null);
	}
}