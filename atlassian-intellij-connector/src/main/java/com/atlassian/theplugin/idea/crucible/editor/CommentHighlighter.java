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

package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.action.crucible.comment.AbstractDiffNavigationAction;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Set;

public final class CommentHighlighter {
	public static final Color VERSIONED_COMMENT_BACKGROUND_COLOR = new Color(0xf2, 0xd0, 0x55);
    public static final Color VERSIONED_READ_COMMENT_BACKGROUND_COLOR = new Color(0xffe897);
	private static final Color VERSIONED_COMMENT_STRIP_MARK_COLOR = VERSIONED_COMMENT_BACKGROUND_COLOR;
    public static final Color VERSIONED_DIRTY_COMMENT_BACKGROUND_COLOR = new Color(199, 200, 200);

	// key injected into document when diff view is opened
	private static final String CRUCIBLE_DATA_KEY_NAME = "CRUCIBLE_DATA_KEY";
	public static final Key<Boolean> CRUCIBLE_DATA_KEY = Key.create(CRUCIBLE_DATA_KEY_NAME);

	private static final String CRUCIBLE_REVIEW_CONTEXT_KEY_NAME = "CRUCIBLE_REVIEW_CONTEXT";
	public static final Key<String> CRUCIBLE_REVIEW_CONTEXT_KEY = Key.create(CRUCIBLE_REVIEW_CONTEXT_KEY_NAME);

	private static final String REVIEW_DATA_KEY_NAME = "REVIEW_DATA_KEY";
	public static final Key<ReviewAdapter> REVIEW_DATA_KEY = Key.create(REVIEW_DATA_KEY_NAME);

	private static final String REVIEWITEM_DATA_KEY_NAME = "REVIEW_ITEM_DATA_KEY";
	public static final Key<CrucibleFileInfo> REVIEWITEM_DATA_KEY = Key.create(REVIEWITEM_DATA_KEY_NAME);

	private static final String REVIEWITEM_CURRENT_CONTENT_KEY_NAME = "EVIEWITEM_CURRENT_CONTENT_KEY";
	public static final Key<Boolean> REVIEWITEM_CURRENT_CONTENT_KEY = Key.create(REVIEWITEM_CURRENT_CONTENT_KEY_NAME);

	private static final String COMMENT_DATA_KEY_NAME = "CRUCIBLE_COMMENT_DATA_KEY";
	public static final Key<Boolean> COMMENT_DATA_KEY = Key.create(COMMENT_DATA_KEY_NAME);

	public static final String VERSIONED_COMMENT_DATA_KEY_NAME = "CRUCIBLE_COMMENT_DATA_KEY";
	public static final Key<VersionedComment> VERSIONED_COMMENT_DATA_KEY = Key.create(VERSIONED_COMMENT_DATA_KEY_NAME);

	private static final Key<DocumentListener> LISTENER_KEY = Key.create("CRUCIBLE_COMMENT_DOCUMENT_LISTENER");

	private static final String FILE_URL = "REVIEW_FILE_URL";
	public static final Key<String> REVIEW_FILE_URL = Key.create(FILE_URL);

	private static final String FILE_REVISION = "REVIEW_FILE_REVISION";
	public static final Key<String> REVIEW_FILE_REVISION = Key.create(FILE_REVISION);


	private CommentHighlighter() {
	}

	public static void highlightCommentsInEditor(@NotNull final Project project,
			@NotNull final Editor editor,
			@NotNull final ReviewAdapter review,
			@NotNull final CrucibleFileInfo reviewItem,
			@Nullable final OpenFileDescriptor displayFile) {
		if (editor != null) {
			Document doc = editor.getDocument();
			VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(doc);
			if (virtualFile != null) {
				if (doc.getUserData(CRUCIBLE_DATA_KEY) == null
						|| doc.getUserData(CRUCIBLE_DATA_KEY)) {
					applyHighlighters(project, editor, review, reviewItem);

					registerKeyboardActions(editor);
					virtualFile.putUserData(REVIEW_DATA_KEY, review);
					virtualFile.putUserData(REVIEWITEM_DATA_KEY, reviewItem);
					virtualFile.putUserData(COMMENT_DATA_KEY, true);
					DocumentListener documentListener = editor.getUserData(LISTENER_KEY);
					if (documentListener == null) {
						documentListener = new DocumentListener() {
							public void beforeDocumentChange(final DocumentEvent event) {
							}

							public void documentChanged(final DocumentEvent event) {
								ApplicationManager.getApplication().invokeLater(new Runnable() {
									public void run() {
                                        Document doc = editor.getDocument();
			                            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(doc);
                                        virtualFile.refresh(true, true);
                                        applyHighlighters(project, editor, review, reviewItem);
									}
								});
							}
						};
						doc.addDocumentListener(documentListener);
						virtualFile.putUserData(LISTENER_KEY, documentListener);
					}
					if (displayFile != null) {
						if (displayFile.canNavigateToSource()) {
							displayFile.navigateIn(editor);
						}
					}
				}
			}
		}
	}

	private static void registerKeyboardActions(Editor editor) {
		final AnAction showNextAction = ActionManager.getInstance().getAction("ThePlugin.Crucible.Comment.NextDiff");
		final AnAction showPrevAction = ActionManager.getInstance().getAction("ThePlugin.Crucible.Comment.PrevDiff");

		if (showNextAction instanceof AbstractDiffNavigationAction) {
			((AbstractDiffNavigationAction) showNextAction).registerShortcutsInEditor(editor);
		}

		if (showPrevAction instanceof AbstractDiffNavigationAction) {
			((AbstractDiffNavigationAction) showPrevAction).registerShortcutsInEditor(editor);
		}
	}

	public static void updateCommentsInEditors(@NotNull final Project project,
			@NotNull final ReviewAdapter review) {

		for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            if (!project.equals(editor.getProject())) {
               continue;
            }
			Document document = editor.getDocument();
			VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
			if (virtualFile != null) {
				if (virtualFile.getUserData(COMMENT_DATA_KEY) != null) {
					final ReviewAdapter data = virtualFile.getUserData(REVIEW_DATA_KEY);
					final CrucibleFileInfo file = virtualFile.getUserData(REVIEWITEM_DATA_KEY);
					if (data != null && file != null) {
						if (data.getPermId().equals(review.getPermId())) {
							try {
								for (CrucibleFileInfo crucibleFileInfo : data.getFiles()) {
									if (crucibleFileInfo.equals(file)) {
										applyHighlighters(project, editor, review, crucibleFileInfo);
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
						if (virtualFile.getUserData(CommentHighlighter.CRUCIBLE_REVIEW_CONTEXT_KEY) != null) {
							final CrucibleFileInfo fileInfo = CodeNavigationUtil.getBestMatchingCrucibleFileInfo(
									virtualFile.getUserData(CommentHighlighter.CRUCIBLE_REVIEW_CONTEXT_KEY), files);
							if (fileInfo != null) {
								CrucibleHelper.openFileWithDiffs(project
										, false
										, review
										, fileInfo
										, 1
										, 1
										,
										new UpdateEditorWithContextActionImpl(project, editor, review,
												fileInfo));
							}
						} else {
							CrucibleFileInfo fileInfo = CodeNavigationUtil.getBestMatchingCrucibleFileInfo(
									virtualFile.getPath(), files);
							if (fileInfo != null && fileInfo.getRepositoryType() != RepositoryType.PATCH) {
								CrucibleHelper.openFileWithDiffs(project
										, false
										, review
										, fileInfo
										, 1
										, 1
										,
										new UpdateEditorCurrentContentActionImpl(project, editor, review,
												fileInfo));
							}
						}
					} catch (ValueNotYetInitialized valueNotYetInitialized) {
						// don't do anything - should not happen, but even if happens - we don't want to break file opening
					}
				}
			}
		}

	}

	public static void removeCommentsInEditors(@NotNull final Project project) {
		for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            if (!project.equals(editor.getProject())) {
                continue;
            }
			Document document = editor.getDocument();
			VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
			if (virtualFile != null) {
				if (virtualFile.getUserData(COMMENT_DATA_KEY) != null) {
					removeHighlightersAndContextData(editor.getDocument().getMarkupModel(project), virtualFile);
				}
			}
		}
	}

	private static void applyHighlighters(@NotNull final Project project,
			@NotNull final Editor editor,
			@NotNull final ReviewAdapter review,
			@NotNull final CrucibleFileInfo fileInfo) {
		final MarkupModel markupModel = editor.getDocument().getMarkupModel(project);
		removeHighlighters(markupModel);

		TextAttributes unreadTextAttributes = new TextAttributes();
        TextAttributes readTextAttributes = new TextAttributes();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (!VcsIdeaHelper.isFileDirty(project, virtualFile)) {
		    unreadTextAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);
            readTextAttributes.setBackgroundColor(VERSIONED_READ_COMMENT_BACKGROUND_COLOR);
        } else {
            unreadTextAttributes.setBackgroundColor(VERSIONED_DIRTY_COMMENT_BACKGROUND_COLOR);
            readTextAttributes.setBackgroundColor(VERSIONED_DIRTY_COMMENT_BACKGROUND_COLOR);
        }
        
		for (VersionedComment comment : fileInfo.getVersionedComments()) {
			int start = 0;
			int stop = 0;
			switch (fileInfo.getCommitType()) {
				case Deleted:
					if (comment.isFromLineInfo()) {
						start = comment.getFromStartLine();
						stop = comment.getFromEndLine();
					}
					break;
				default:
					if (comment.isToLineInfo()) {
						start = comment.getToStartLine();
						stop = comment.getToEndLine();
					}
					break;
			}
			if (start > 0) {
				int endLine = stop > 0 ? stop : start;
				try {
					final int startOffset = editor.getDocument().getLineStartOffset(start - 1);
					int endOffset = editor.getDocument().getLineEndOffset(endLine - 1);
					if (startOffset < endOffset) {
						endOffset--;
					}
                    if (editor.getDocument().getModificationStamp() > 0) {
                        int i = 1;
                    }
                    boolean unread = comment.getReadState() == Comment.ReadState.UNREAD
                            || comment.getReadState() == Comment.ReadState.LEAVE_UNREAD;
					RangeHighlighter rh = markupModel.addRangeHighlighter(startOffset, endOffset,
							HighlighterLayer.SELECTION - 1, unread ? unreadTextAttributes : readTextAttributes,
                            HighlighterTargetArea.LINES_IN_RANGE);
//					rh.setErrorStripeTooltip("<html><b>" + comment.getAuthor().getDisplayName()
//							+ ":</b> " + comment.getMessage());
					rh.setErrorStripeMarkColor(VERSIONED_COMMENT_STRIP_MARK_COLOR);
					rh.setGutterIconRenderer(unread
                            ? new UnreadCrucibleGutterIconRenderer(editor, review, fileInfo, comment)
                            : new ReadCrucibleGutterIconRenderer(editor, review, fileInfo, comment));
					rh.putUserData(COMMENT_DATA_KEY, true);
					rh.putUserData(VERSIONED_COMMENT_DATA_KEY, comment);
				} catch (Exception e) {
					PluginUtil.getLogger().error(e);
				}
			}
		}
	}

	private static void removeHighlighters(@NotNull final MarkupModel markupModel) {
		for (RangeHighlighter rh : markupModel.getAllHighlighters()) {
			if (rh.getUserData(COMMENT_DATA_KEY) != null) {
				rh.putUserData(COMMENT_DATA_KEY, null);
				rh.putUserData(VERSIONED_COMMENT_DATA_KEY, null);
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
