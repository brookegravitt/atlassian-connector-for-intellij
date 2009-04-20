package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.intellij.openapi.diff.FileContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class UpdateEditorWithContextActionImpl implements OpenDiffAction {
	private final Project project;
	private final Editor editor;
	private final ReviewAdapter review;
	private final CrucibleFileInfo reviewItem;

	public UpdateEditorWithContextActionImpl(@NotNull final Project project,
			@NotNull Editor editor,
			@NotNull final ReviewAdapter review,
			@NotNull final CrucibleFileInfo reviewItem) {
		this.project = project;
		this.editor = editor;
		this.review = review;
		this.reviewItem = reviewItem;
	}

	public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType) {
		switch (commitType) {
			case Moved:
			case Copied:
			case Modified:
				if (displayFile != null) {
					displayFile.getFile().putUserData(CommentHighlighter.REVIEWITEM_DATA_KEY, reviewItem);
					if (referenceFile != null) {
						final Document displayDocument = new FileContent(project, displayFile.getFile())
								.getDocument();
						final Document referenceDocument = new FileContent(project, referenceFile).getDocument();
						ChangeViewer.highlightChangesInEditor(project, referenceDocument, displayDocument
								, reviewItem.getOldFileDescriptor().getRevision()
								, reviewItem.getFileDescriptor().getRevision());
					}
					CommentHighlighter.highlightCommentsInEditor(project, editor, review, reviewItem, null);
				}
				break;
			case Added:
				if (displayFile != null) {
					displayFile.getFile().putUserData(CommentHighlighter.REVIEWITEM_DATA_KEY, reviewItem);
					CommentHighlighter.highlightCommentsInEditor(project, editor, review, reviewItem, null);
				}
				break;
			case Deleted:
				if (referenceFile != null) {
					referenceFile.putUserData(CommentHighlighter.REVIEWITEM_DATA_KEY, reviewItem);
					CommentHighlighter
							.highlightCommentsInEditor(project, editor, review, reviewItem, null);
				}
				break;
			default:
				break;
		}

	}
}