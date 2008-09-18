package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.CommentTreePanel;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;

public abstract class AbstractCrucibleFileAction extends ReviewTreeAction {
	@Override
	public void update(final AnActionEvent e) {
		boolean enabled = e.getData(Constants.CRUCIBLE_FILE_NODE_KEY) != null;
		e.getPresentation().setEnabled(enabled);

		if (e.getPlace().equals(CommentTreePanel.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
	}

}
