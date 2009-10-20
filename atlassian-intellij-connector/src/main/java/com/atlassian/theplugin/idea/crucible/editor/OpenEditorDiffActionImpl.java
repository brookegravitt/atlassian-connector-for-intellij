package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.intellij.openapi.diff.FileContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class OpenEditorDiffActionImpl implements OpenDiffAction {
	private final Project project;
	private final ReviewAdapter review;
	private final CrucibleFileInfo reviewItem;
	private final int line;
	private final int column;
	private final boolean focusOnOpen;

	public OpenEditorDiffActionImpl(@NotNull final Project project,
			@NotNull final ReviewAdapter review,
			@NotNull final CrucibleFileInfo reviewItem,
			final boolean focusOnOpen) {
		this.project = project;
		this.review = review;
		this.reviewItem = reviewItem;
		this.focusOnOpen = focusOnOpen;
		this.line = 1;
		this.column = 1;
	}

	public OpenEditorDiffActionImpl(@NotNull final Project project,
			@NotNull final ReviewAdapter review,
			@NotNull final CrucibleFileInfo reviewItem,
			final int line,
			final int column,
			final boolean focusOnOpen) {
		this.project = project;
		this.review = review;
		this.reviewItem = reviewItem;
		this.line = line;
		this.column = column;
		this.focusOnOpen = focusOnOpen;
	}

	public Project getProject() {
		return project;
	}

	public boolean isFocusOnOpen() {
		return focusOnOpen;
	}

	public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType) {
		FileEditorManager fem = FileEditorManager.getInstance(project);
		switch (commitType) {
			case Moved:
			case Copied:
			case Modified:
				if (displayFile != null) {
					displayFile.getFile().putUserData(CommentHighlighter.CRUCIBLE_REVIEW_CONTEXT_KEY,
							reviewItem.getFileDescriptor().getUrl());
					displayFile.getFile().putUserData(CommentHighlighter.REVIEWITEM_DATA_KEY, reviewItem);
					Editor editor = fem.openTextEditor(displayFile, focusOnOpen);
					if (editor == null) {
						return;
					}

					if (referenceFile != null
                            && reviewItem.getOldFileDescriptor() != null
                            && reviewItem.getFileDescriptor() != null) {

						final Document displayDocument = new FileContent(project, displayFile.getFile()).getDocument();
						final Document referenceDocument = new FileContent(project, referenceFile).getDocument();

                        ChangeViewer.highlightChangesInEditor(project, referenceDocument, displayDocument
								, reviewItem.getOldFileDescriptor().getRevision()
								, reviewItem.getFileDescriptor().getRevision());
					}
					CommentHighlighter.highlightCommentsInEditor(project, editor, review, reviewItem, displayFile);
				}
				break;
			case Added:
				if (displayFile != null) {
					displayFile.getFile().putUserData(CommentHighlighter.CRUCIBLE_REVIEW_CONTEXT_KEY,
							reviewItem.getFileDescriptor().getUrl());
					displayFile.getFile().putUserData(CommentHighlighter.REVIEWITEM_DATA_KEY, reviewItem);
					Editor editor = fem.openTextEditor(displayFile, focusOnOpen);
					if (editor == null) {
						return;
					}
					CommentHighlighter.highlightCommentsInEditor(project, editor, review, reviewItem, displayFile);
				}
				break;
			case Deleted:
				if (referenceFile != null) {
					referenceFile.putUserData(CommentHighlighter.CRUCIBLE_REVIEW_CONTEXT_KEY,
							reviewItem.getOldFileDescriptor().getUrl());
					referenceFile.putUserData(CommentHighlighter.REVIEWITEM_DATA_KEY, reviewItem);
					OpenFileDescriptor referenceFileDescriptor = new OpenFileDescriptor(project, referenceFile, line, column);
					Editor editor = fem.openTextEditor(referenceFileDescriptor, focusOnOpen);
					if (editor == null) {
						return;
					}
					CommentHighlighter.highlightCommentsInEditor(project, editor, review, reviewItem, referenceFileDescriptor);
				}
				break;
			default:
				break;
		}

	}
}
