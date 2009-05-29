package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class GoToLocalSourceAction extends ReviewTreeAction {
	protected void executeTreeAction(final Project project, final AtlassianTreeWithToolbar tree) {
		final ReviewActionData actionData = new ReviewActionData(tree);
		final PsiFile psiFile = CodeNavigationUtil.guessCorrespondingPsiFile(project,
				actionData.file.getFileDescriptor().getAbsoluteUrl());

        AtlassianTreeNode node = tree.getSelectedTreeNode();

		if (psiFile != null) {
            if (node instanceof VersionedCommentTreeNode) {
                VersionedCommentTreeNode vctn = (VersionedCommentTreeNode) node;
                VersionedComment comment = vctn.getComment();
                VirtualFile vf = psiFile.getVirtualFile();
                if (vf != null) {
                    OpenFileDescriptor display = new OpenFileDescriptor(project, vf, comment.getToStartLine() - 1, 0);
                    if (display.canNavigateToSource()) {
                        display.navigate(false);
                    }
                }
            } else {
			    psiFile.navigate(true);
            }
		}
	}
}
