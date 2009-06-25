package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;

/**
 * User: kalamon
 * Date: Jun 25, 2009
 * Time: 1:17:10 PM
 */
public abstract class ReviewTreeItemActionWithPatchChecking extends ReviewTreeAction {
    @Override
    protected void updateTreeAction(AnActionEvent e, AtlassianTreeWithToolbar tree) {
        super.updateTreeAction(e, tree);
        if (e.getPresentation().isEnabled()) {
            AtlassianTreeNode node = tree.getSelectedTreeNode();
            if (node instanceof VersionedCommentTreeNode) {
                VersionedCommentTreeNode vctn = (VersionedCommentTreeNode) node;
                if (vctn.getFile().getRepositoryType() == RepositoryType.PATCH) {
                    e.getPresentation().setEnabled(false);
                }
            } else if (node instanceof CrucibleFileNode) {
                if (((CrucibleFileNode) node).getFile().getRepositoryType() == RepositoryType.PATCH) {
                    e.getPresentation().setEnabled(false);
                }
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
        e.getPresentation().setVisible(e.getPresentation().isEnabled());
    }
}
