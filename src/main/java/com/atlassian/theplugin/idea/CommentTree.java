package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 10:39:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentTree extends AtlassianTree {
	public CommentTree() {
		super();
		putClientProperty("JTree.lineStyle", "None");
		setShowsRootHandles(false);
		setRootVisible(false);
		setRowHeight(100);
	}
}
