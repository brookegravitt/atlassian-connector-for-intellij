package com.atlassian.theplugin.idea.action.tree.file;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * User: jgorycki
 * Date: Feb 3, 2009
 * Time: 3:46:33 PM
 */
public class ExpandFilesAction extends TreeAction {
	protected void executeTreeAction(final Project project, AtlassianTreeWithToolbar tree) {
		if (tree == null) {
			tree = IdeaHelper.getCrucibleToolWindow(project).getAtlassianTreeWithToolbar();
		}
		if (tree != null) {
			tree.setViewState(AtlassianTreeWithToolbar.ViewState.EXPANDED);
		}
	}

	protected void updateTreeAction(final AnActionEvent e, AtlassianTreeWithToolbar tree) {
	}

}
