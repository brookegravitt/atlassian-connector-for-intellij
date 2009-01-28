package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

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
		Document doc = editorFactoryEvent.getEditor().getDocument();
		VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(doc);
		if (virtualFile != null) {
//			CrucibleFileInfo crucibleFileInfo = virtualFile.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);
//			if (crucibleFileInfo == null) {
			Collection<ReviewAdapter> reviews = crucibleReviewListModel.getOpenInIdeReviews();
			if (!reviews.isEmpty()) {
				for (ReviewAdapter review : reviews) {
					try {
						CrucibleFileInfo crucibleFile = CodeNavigationUtil
								.getBestMatchingCrucibleFileInfo(virtualFile.getPath(), review.getFiles());
						if (crucibleFile != null) {
							showVirtualFileWithComments(project, editorFactoryEvent.getEditor(), virtualFile,
									review,
									crucibleFile);
						}
					} catch (ValueNotYetInitialized valueNotYetInitialized) {
						// don't do anything - should not happen, but even if happens - we don't want to break file opening
					}
				}
			}
//			}
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

		VcsIdeaHelper.openFileWithDiffs(project
				, false
				, reviewItem.getFileDescriptor().getAbsoluteUrl()
				, reviewItem.getOldFileDescriptor().getRevision()
				, reviewItem.getFileDescriptor().getRevision()
				, reviewItem.getCommitType()
				, line
				, 1
				, new UpdateEditorDiffActionImpl(project, editor, review, reviewItem));
	}
}
