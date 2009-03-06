package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.bamboo.OpenBambooDiffToolAction;
import com.atlassian.theplugin.idea.ui.tree.file.BambooFileNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.util.Iterator;
import java.util.List;

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
			VcsFileRevision vcsFileRevision = iterator.next();
			if (vcsFileRevision.getRevisionNumber().asString().equals(bambooFileNode.getRevision())) {
				if (iterator.hasNext()) {
					final String prevRevision = iterator.next().getRevisionNumber().asString();
					VcsIdeaHelper.openFileWithDiffs(project, true, virtualFile.getPath(),
							prevRevision, bambooFileNode.getRevision(),
							CommitType.Modified, 1, 1,
							new OpenBambooDiffToolAction(project, bambooFileNode.getName(), prevRevision,
									bambooFileNode.getRevision()) {

							});
				} else {
					Messages.showInfoMessage(project, "This file does not have any revisions before "
							+ bambooFileNode.getRevision(), "Information");
				}
				break;
			}
		}
	}

	public void actionPerformed(AnActionEvent event) {
		showDiff(IdeaHelper.getCurrentProject(event), getBambooFileNode(event));
	}
}
