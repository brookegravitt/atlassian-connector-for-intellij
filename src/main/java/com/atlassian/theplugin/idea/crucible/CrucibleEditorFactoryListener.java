package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.diff.FileContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class CrucibleEditorFactoryListener implements EditorFactoryListener {
	private final Project project;
	private final CrucibleReviewListModel crucibleReviewListModel;

	public CrucibleEditorFactoryListener(@NotNull final Project project,
			@NotNull final CrucibleReviewListModel crucibleReviewListModel) {
		this.project = project;
		this.crucibleReviewListModel = crucibleReviewListModel;
	}

	public void editorCreated(final EditorFactoryEvent editorFactoryEvent) {
		Collection<ReviewAdapter> reviews = crucibleReviewListModel.getOpenInIdeReviews();
		if (!reviews.isEmpty()) {
			for (ReviewAdapter review : reviews) {
				try {
					Set<CrucibleFileInfo> files = review.getFiles();
					for (CrucibleFileInfo fileInfo : files) {
						PsiFile f = CodeNavigationUtil
								.guessCorrespondingPsiFile(project, fileInfo.getFileDescriptor().getName());
						if (f != null) {
							Document doc = editorFactoryEvent.getEditor().getDocument();
							VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(doc);
							if (virtualFile.equals(f.getVirtualFile())) {
								if (fileInfo.getNumberOfLineComments() > 0) {
									showVirtualFileWithComments(project, editorFactoryEvent.getEditor(), virtualFile, review,
											fileInfo);
								}
							}

						}
					}
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
					// don't do anything - should not happen, but even if happens - we don't want to break file opening
				}
			}
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
				, new VcsIdeaHelper.OpenDiffAction() {

					public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType) {
						if (referenceFile == null) {
							Messages.showErrorDialog(project,
									"Cannot fetch " + reviewItem.getOldFileDescriptor().getAbsoluteUrl()
											+ ".\nAnnotated file cannot be displayed.", "Error");
							return;
						}

						switch (commitType) {
							case Moved:
							case Copied:
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
				});
	}
}
