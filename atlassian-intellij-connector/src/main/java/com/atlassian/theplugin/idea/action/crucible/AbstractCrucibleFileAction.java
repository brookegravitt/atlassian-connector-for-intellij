package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.intellij.openapi.actionSystem.AnActionEvent;

public abstract class AbstractCrucibleFileAction extends ReviewTreeItemActionWithPatchChecking {
	@Override
    protected void updateTreeAction(AnActionEvent e, AtlassianTreeWithToolbar tree) {
        super.updateTreeAction(e, tree);
        boolean enabled = e.getData(Constants.CRUCIBLE_FILE_NODE_KEY) != null && e.getPresentation().isEnabled();
        e.getPresentation().setEnabled(enabled);

        if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
            e.getPresentation().setVisible(enabled);
        }
	}
}
