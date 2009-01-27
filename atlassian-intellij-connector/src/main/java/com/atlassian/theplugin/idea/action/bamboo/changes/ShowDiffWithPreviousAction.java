package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.crucible.editor.OpenDiffAction;
import com.atlassian.theplugin.idea.ui.tree.file.BambooFileNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diff.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.psi.PsiFile;

import java.util.List;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

/**
 * User: jgorycki
 * Date: Jan 27, 2009
 * Time: 3:32:39 PM
 */
public class ShowDiffWithPreviousAction extends AbstractBambooFileActions {
	
	public void showDiff(final Project project, final BambooFileNode bambooFileNode) {
		final PsiFile psiFile = bambooFileNode.getPsiFile();
		if (psiFile == null) {
			Messages.showErrorDialog(project, "Cannot find corresponding file in the project.", "Problem");
			return;
		}
		final VirtualFile virtualFile = psiFile.getVirtualFile();
		if (virtualFile == null) {
			Messages.showErrorDialog(project, "PsiFile has not corresponding VirtualFile.", "Problem");
			return;
		}
		final VcsRevisionNumber currentRevisionNumber = VcsIdeaHelper.getVcsRevisionNumber(project, virtualFile);
		if (currentRevisionNumber == null) {
			Messages.showErrorDialog(project, "Cannot determine current version of file ["
					+ psiFile.getName() + "] in the project.", "Problem");
			return;
		}

		List<VcsFileRevision> history = null;
		try {
			history = VcsIdeaHelper.getFileHistory(project, virtualFile);
			if (history == null) {
				return;
			}
		} catch (VcsException e) {
			Messages.showErrorDialog(project, "Error getting file history: " + e.getMessage(), "Problem");
			return;
		}

		for (Iterator<VcsFileRevision> iterator = history.iterator(); iterator.hasNext();) {
			VcsFileRevision vcsFileRevision =  iterator.next();
			if (vcsFileRevision.getRevisionNumber().asString().equals(bambooFileNode.getRevision())) {
				if (iterator.hasNext()) {
					final String prevRevision = iterator.next().getRevisionNumber().asString();
					VcsIdeaHelper.openFileWithDiffs(project, true, virtualFile.getPath(),
							prevRevision, bambooFileNode.getRevision(),
							CommitType.Modified, 1, 1, new MyOpenDiffAction(project, bambooFileNode, prevRevision) {

							});
				} else {
					Messages.showInfoMessage(project, "This file does not have any revisions before "
							+ bambooFileNode.getRevision(), "Information");
				}
				break;
			}
		}
	}

	private class MyOpenDiffAction implements OpenDiffAction {
		private final Document emptyDocument = new DocumentImpl("");
		private Project project;
		private BambooFileNode bambooFileNode;
		private String prevRevision;

		public MyOpenDiffAction(Project project, BambooFileNode bambooFileNode, String prevRevision) {
			this.project = project;
			this.bambooFileNode = bambooFileNode;
			this.prevRevision = prevRevision;
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
				displayDocument = displayFileContent.getDocument();
			}

			DiffContent referenceFileContent = null;
			if (referenceFile != null) {
				referenceFileContent = createDiffContent(project, referenceFile);
				referenceDocument = referenceFileContent.getDocument();
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
							VcsBundle.message("diff.content.title.repository.version", prevRevision),
							VcsBundle.message("diff.content.title.repository.version",
									bambooFileNode.getRevision())
					});
				}

				@Override
				public String getWindowTitle() {
					return bambooFileNode.getName();
				}
			};
			DiffManager.getInstance().getDiffTool().show(request);
		}
	}

	public void actionPerformed(AnActionEvent event) {
		showDiff(IdeaHelper.getCurrentProject(event), getBambooFileNode(event));
	}
}
