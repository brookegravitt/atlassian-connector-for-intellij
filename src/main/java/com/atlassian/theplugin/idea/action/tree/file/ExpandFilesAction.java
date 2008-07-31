package com.atlassian.theplugin.idea.action.tree.file;

import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 30, 2008
 * Time: 4:18:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExpandFilesAction extends TreeAction {
    protected void executeTreeAction(final AtlassianTreeWithToolbar tree) {
        tree.expandAll();
    }
}
