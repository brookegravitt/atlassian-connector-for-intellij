package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.action.tree.file.TreeAction;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleChangeSetTitleNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.tree.TreeNode;

public abstract class ReviewTreeAction extends TreeAction {

	protected abstract void executeTreeAction(final AtlassianTreeWithToolbar tree);

	protected void updateTreeAction(final AnActionEvent e, final AtlassianTreeWithToolbar tree) {
		ReviewActionData actionData = new ReviewActionData(tree);
		if (actionData.review != null && actionData.file != null) {
			e.getPresentation().setEnabled(true);
		} else {
			e.getPresentation().setEnabled(false);
		}
	}

	protected static class ReviewActionData {
		protected final ReviewData review;
		protected final CrucibleFileInfo file;

		ReviewActionData(AtlassianTreeWithToolbar tree) {
			AtlassianTreeNode node = tree.getSelectedTreeNode();
			if (node != null) {
				if (node instanceof CrucibleFileNode) {
					file = ((CrucibleFileNode) node).getFile();
				} else {
					file = null;
				}
				ReviewData rd = null;
				for (TreeNode n : node.getPath()) {
					if (n instanceof CrucibleChangeSetTitleNode) {
						rd = ((CrucibleChangeSetTitleNode) n).getReview();
						break;
					}
				}
				review = rd;
			} else {
				review = null;
				file = null;
			}
		}
	}
}