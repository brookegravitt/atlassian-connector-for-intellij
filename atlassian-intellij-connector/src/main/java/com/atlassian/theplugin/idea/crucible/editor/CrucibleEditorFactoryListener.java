package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;

public class CrucibleEditorFactoryListener implements EditorFactoryListener {
	private final Project project;
	private final CrucibleReviewListModel crucibleReviewListModel;

	public CrucibleEditorFactoryListener(@NotNull final Project project,
			@NotNull final CrucibleReviewListModel crucibleReviewListModel) {
		this.project = project;
		this.crucibleReviewListModel = crucibleReviewListModel;
	}

	public void editorCreated(final EditorFactoryEvent editorFactoryEvent) {
		try {
			Document doc = editorFactoryEvent.getEditor().getDocument();
			VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(doc);
			if (virtualFile != null && project.equals(editorFactoryEvent.getEditor().getProject())) {
				CrucibleFileInfo crucibleFile = virtualFile.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);
				if (crucibleFile == null) {
					Collection<ReviewAdapter> reviews = crucibleReviewListModel.getOpenInIdeReviews();
					if (!reviews.isEmpty()) {
						for (ReviewAdapter review : reviews) {
							crucibleFile = CodeNavigationUtil.getBestMatchingCrucibleFileInfo(virtualFile.getPath(), review
									.getFiles());

							if (crucibleFile != null) {
								showVirtualFileWithComments(project, editorFactoryEvent.getEditor(), virtualFile,
										review, crucibleFile);
							}
						}
					}
				}
			}
		} catch (Throwable t) {
			// this is crutial part of file open procedure in IDEA. Even if we can not display comment highlighters
			// file should be opened anyway.
			LoggerImpl.getInstance().error("Unable to determine Crucible comments for file", t);
		}
	}

	public void editorReleased(final EditorFactoryEvent editorFactoryEvent) {
	}

	private static void showVirtualFileWithComments(@NotNull final Project project,
			@NotNull final Editor editor,
			@NotNull final VirtualFile virtualFile,
			@NotNull final ReviewAdapter review,
			@NotNull final CrucibleFileInfo reviewItem) {

		int line = 1;
		VersionedComment comment = virtualFile.getUserData(CommentHighlighter.VERSIONED_COMMENT_DATA_KEY);
		if (comment != null) {
			line = comment.getToStartLine();
		}
		CrucibleHelper.openFileWithDiffs(project
				, false
				, review
				, reviewItem
				, line
				, 1,
				new UpdateEditorCurrentContentActionImpl(project, editor, review, reviewItem));
	}
}
