package com.atlassian.theplugin.idea.action.tree.file;

import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 30, 2008
 * Time: 4:18:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExpandCollapseFilesAction extends TreeAction {
    protected void executeTreeAction(final AtlassianTreeWithToolbar tree) {
        tree.setViewState(tree.getViewState().getNextState());
    }

    protected void updateTreeAction(final AnActionEvent e, final AtlassianTreeWithToolbar tree) {
        e.getPresentation().setIcon(tree.getViewState().getNextState().getIcon());
        e.getPresentation().setText(tree.getViewState().getNextState().toString());
    }
}
