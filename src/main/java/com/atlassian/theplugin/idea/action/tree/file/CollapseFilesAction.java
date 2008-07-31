package com.atlassian.theplugin.idea.action.tree.file;

import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 30, 2008
 * Time: 4:18:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollapseFilesAction extends TreeAction {
    protected void executeTreeAction(final AtlassianTreeWithToolbar tree) {
        tree.collapseAll();
    }
}
