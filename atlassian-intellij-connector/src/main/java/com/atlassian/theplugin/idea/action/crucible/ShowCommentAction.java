package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.ui.tree.clickaction.CrucibleVersionedCommentClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;

/**
 * User: kalamon
 * Date: May 29, 2009
 * Time: 12:12:15 PM
 */
public class ShowCommentAction extends ReviewTreeItemActionWithPatchChecking {
    protected void executeTreeAction(Project project, AtlassianTreeWithToolbar tree) {
        AtlassianTreeNode node = tree.getSelectedTreeNode();
        if (node instanceof VersionedCommentTreeNode) {
            new CrucibleVersionedCommentClickAction(project).execute(node, 2);
        }
    }

    @Override
    protected void updateTreeAction(AnActionEvent e, AtlassianTreeWithToolbar tree) {
        super.updateTreeAction(e, tree);
        if (e.getPresentation().isEnabled()) {
            boolean enabled = e.getData(Constants.CRUCIBLE_VERSIONED_COMMENT_NODE_KEY) != null;
            e.getPresentation().setEnabled(enabled);

            if (e.getPlace().equals(CrucibleConstants.MENU_PLACE) || (e.getPlace().equals(ReviewItemTreePanel.MENU_PLACE))) {
                e.getPresentation().setVisible(enabled);
            }
        }
    }
}
