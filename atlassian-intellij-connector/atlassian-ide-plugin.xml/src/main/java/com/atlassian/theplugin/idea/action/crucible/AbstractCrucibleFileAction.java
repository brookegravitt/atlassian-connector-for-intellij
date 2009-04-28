package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

public abstract class AbstractCrucibleFileAction extends ReviewTreeAction {
	@Override
	public void update(final AnActionEvent e) {
		boolean enabled = e.getData(Constants.CRUCIBLE_FILE_NODE_KEY) != null;
		e.getPresentation().setEnabled(enabled);

		if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
			e.getPresentation().setVisible(enabled);
		}
	}

}