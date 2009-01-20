package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.ChangeViewer;
import com.atlassian.theplugin.idea.crucible.CommentHighlighter;
import com.intellij.openapi.diff.FileContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class EditorDifferenceActionImpl implements OpenDiffAction {
	private final Project project;
	private final ReviewAdapter review;
	private final CrucibleFileInfo reviewItem;



	public EditorDifferenceActionImpl(@NotNull final Project project,
			@NotNull final ReviewAdapter review,
			@NotNull final CrucibleFileInfo reviewItem) {
		this.project = project;
		this.review = review;
		this.reviewItem = reviewItem;
	}

	public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType) {
		FileEditorManager fem = FileEditorManager.getInstance(project);
		Editor editor = fem.openTextEditor(displayFile, true);
		if (editor == null) {
			return;
		}
		switch (commitType) {

			case Copied:
				case Moved:
			case Modified:
				final Document displayDocument = new FileContent(project, displayFile.getFile())
						.getDocument();
				final Document referenceDocument = new FileContent(project, referenceFile).getDocument();
				ChangeViewer.highlightChangesInEditor(project, /*editor, */referenceDocument, displayDocument
						, reviewItem.getOldFileDescriptor().getRevision()
						, reviewItem.getFileDescriptor().getRevision());
				break;
			case Added:
				break;
			case Deleted:
				break;
			default:
				break;
		}
		CommentHighlighter.highlightCommentsInEditor(project, editor, review, reviewItem, displayFile);
	}
}
