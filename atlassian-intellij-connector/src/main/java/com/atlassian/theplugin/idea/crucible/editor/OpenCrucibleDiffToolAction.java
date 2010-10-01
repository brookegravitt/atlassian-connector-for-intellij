package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.AbstractOpenDiffToolAction;
import com.intellij.openapi.diff.DiffContent;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.DocumentContent;
import com.intellij.openapi.diff.FileContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vfs.VirtualFile;

public class OpenCrucibleDiffToolAction extends AbstractOpenDiffToolAction {
    private ReviewAdapter review;
    private final CrucibleFileInfo reviewItem;

	public OpenCrucibleDiffToolAction(final Project project, ReviewAdapter review, final CrucibleFileInfo reviewItem) {
		super(project, reviewItem.getFileDescriptor().getAbsoluteUrl(), reviewItem.getOldFileDescriptor().getRevision(),
				reviewItem.getFileDescriptor().getRevision());
        this.review = review;
        this.reviewItem = reviewItem;
	}

    @Override
    public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType) {
        FileEditorManager fem = FileEditorManager.getInstance(getProject());
        if (displayFile != null) {

            displayFile.getFile().putUserData(CommentHighlighter.CRUCIBLE_REVIEW_CONTEXT_KEY,
                    reviewItem.getFileDescriptor().getUrl());
            displayFile.getFile().putUserData(CommentHighlighter.REVIEWITEM_DATA_KEY, reviewItem);

            Editor editor = fem.openTextEditor(displayFile, false);
            if (editor != null) {
                if (referenceFile != null
                        && reviewItem.getOldFileDescriptor() != null
                        && reviewItem.getFileDescriptor() != null) {

                    final Document displayDocument = new FileContent(getProject(), displayFile.getFile()).getDocument();
                    final Document referenceDocument = new FileContent(getProject(), referenceFile).getDocument();

                    ChangeViewer.highlightChangesInEditor(getProject(), referenceDocument, displayDocument
                            , reviewItem.getOldFileDescriptor().getRevision()
                            , reviewItem.getFileDescriptor().getRevision());
                }

                CommentHighlighter.highlightCommentsInEditor(getProject(), editor, review, reviewItem, displayFile);
            }
        }

        super.run(displayFile, referenceFile, commitType);
    }

    protected DiffRequest getDiffRequest(final Project aProject, final DocumentContent referenceDoc,
			final DocumentContent displayDoc) {
		return new DiffRequest(aProject) {
			@Override
			public DiffContent[] getContents() {
				return (new DiffContent[]{
						referenceDoc,
						displayDoc
				});
			}

			@Override
			public String[] getContentTitles() {
				switch (reviewItem.getRepositoryType()) {
					case SCM:
						return (new String[]{
								VcsBundle.message("diff.content.title.repository.version",
										fromRevision),
								VcsBundle.message("diff.content.title.repository.version",
										toRevision)
						});
					default:
						return (new String[]{
								"Before change",
								"After change"});
				}
			}

			@Override
			public String getWindowTitle() {
				return reviewItem.getFileDescriptor().getAbsoluteUrl();
			}
		};


	}
}

