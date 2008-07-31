package com.atlassian.theplugin.idea.action.tree.file;

import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 31, 2008
 * Time: 8:35:51 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TreeAction extends AnAction {
    public void actionPerformed(final AnActionEvent e) {
        Component component = DataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());
        Container parent = component.getParent();
        while (parent != null) {
            if (parent instanceof AtlassianTreeWithToolbar) {
                break;
            }
            parent = parent.getParent();
        }
        if (parent == null) {
            return;
        }
        AtlassianTreeWithToolbar tree = (AtlassianTreeWithToolbar) parent;
        executeTreeAction(tree);
    }

    protected abstract void executeTreeAction(AtlassianTreeWithToolbar tree);
}
