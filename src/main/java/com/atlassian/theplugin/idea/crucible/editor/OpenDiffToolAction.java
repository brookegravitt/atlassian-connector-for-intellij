package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
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
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class OpenDiffToolAction implements OpenDiffAction {
	final Document emptyDocument = new DocumentImpl("");

	private final Project project;
	private final CrucibleFileInfo reviewItem;

	public OpenDiffToolAction(final Project project, final CrucibleFileInfo reviewItem) {
		this.project = project;
		this.reviewItem = reviewItem;
	}

	@NotNull
	private DiffContent createDiffContent(@NotNull final Project aProject, @NotNull final VirtualFile virtualFile) {
		if (!FileTypeManager.getInstance().getFileTypeByFile(virtualFile).isBinary()) {
			return new FileContent(aProject, virtualFile);
		} else {
			return IdeaVersionFacade.getInstance().createBinaryContent(virtualFile);
		}
	}

	public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType) {
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

		DiffRequest request = new DiffRequest(project) {
			@Override
			public DiffContent[] getContents() {
				return (new DiffContent[]{
						referenceDocumentContentFinal,
						displayDocumentContentFinal
				});
			}

			@Override
			public String[] getContentTitles() {
				return (new String[]{
						VcsBundle.message("diff.content.title.repository.version",
								reviewItem.getOldFileDescriptor().getRevision()),
						VcsBundle.message("diff.content.title.repository.version",
								reviewItem.getFileDescriptor().getRevision())
				});
			}

			@Override
			public String getWindowTitle() {
				return reviewItem.getFileDescriptor().getAbsoluteUrl();
			}
		};
		DiffManager.getInstance().getDiffTool().show(request);
	}
}

