package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.editor.OpenDiffAction;
import com.intellij.openapi.diff.DiffContent;
import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.DocumentContent;
import com.intellij.openapi.diff.FileContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractOpenDiffToolAction implements OpenDiffAction {
	private final Document emptyDocument = new DocumentImpl("");
	private final Project project;
	protected final String filename;
	protected final String fromRevision;
	protected final String toRevision;

	public AbstractOpenDiffToolAction(final Project project, final String filename, final String fromRevision,
			final String toRevision) {
		this.project = project;
		this.filename = filename;
		this.fromRevision = fromRevision;
		this.toRevision = toRevision;
	}

    protected Project getProject() {
        return project;
    }

    @NotNull
	private DiffContent createDiffContent(@NotNull final Project aProject, @NotNull final VirtualFile virtualFile) {
		if (!FileTypeManager.getInstance().getFileTypeByFile(virtualFile).isBinary()) {
			return new FileContent(aProject, virtualFile);
		} else {
			return IdeaVersionFacade.getInstance().createBinaryContent(virtualFile);
		}
	}

	public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, final CommitType commitType) {
		Document displayDocument = emptyDocument;
		Document referenceDocument = emptyDocument;
		displayDocument.setReadOnly(true);
		referenceDocument.setReadOnly(true);
		DiffContent displayFileContent = null;
		if (displayFile != null) {
			displayFileContent = createDiffContent(project, displayFile.getFile());
			if (displayFileContent != null) {
				displayDocument = displayFileContent.getDocument();
			}
		}

		DiffContent referenceFileContent = null;
		if (referenceFile != null) {
			referenceFileContent = createDiffContent(project, referenceFile);
			if (referenceFileContent != null) {
				referenceDocument = referenceFileContent.getDocument();
			}
		}

		if ((displayFileContent != null && displayFileContent.isBinary())
				|| (referenceFileContent != null && referenceFileContent.isBinary())) {
			Messages.showInfoMessage(project, "Files are binary. Diff not available.", "Information");
			return;
		}

		final DocumentContent displayDocumentContentFinal = new DocumentContent(project, displayDocument);
		final DocumentContent referenceDocumentContentFinal = new DocumentContent(project, referenceDocument);

		if (!displayDocument.equals(emptyDocument)) {
			displayDocumentContentFinal.getDocument().putUserData(CommentHighlighter.CRUCIBLE_DATA_KEY, true);
			referenceDocumentContentFinal.getDocument().putUserData(CommentHighlighter.CRUCIBLE_DATA_KEY, false);
		} else {
			if (!referenceDocumentContentFinal.equals(emptyDocument)) {
				referenceDocumentContentFinal.getDocument().putUserData(CommentHighlighter.CRUCIBLE_DATA_KEY, true);
				displayDocumentContentFinal.getDocument().putUserData(CommentHighlighter.CRUCIBLE_DATA_KEY, false);
			}
		}

		DiffRequest request = getDiffRequest(project, referenceDocumentContentFinal, displayDocumentContentFinal);
		DiffManager.getInstance().getDiffTool().show(request);
	}

	protected abstract DiffRequest getDiffRequest(final Project aProject, final DocumentContent referenceDoc,
			final DocumentContent displayDoc);
}