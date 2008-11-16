package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public class GoToLocalSourceAction extends AbstractCrucibleFileAction {
	protected void executeTreeAction(final Project project, final AtlassianTreeWithToolbar tree) {
		final ReviewActionData actionData = new ReviewActionData(tree);
		final PsiFile psiFile = CodeNavigationUtil.guessCorrespondingPsiFile(project,
				actionData.file.getFileDescriptor().getAbsoluteUrl());
		if (psiFile != null) {
			psiFile.navigate(true);
		}
	}
}
