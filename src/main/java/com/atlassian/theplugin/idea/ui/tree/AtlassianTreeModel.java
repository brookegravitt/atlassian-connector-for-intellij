package com.atlassian.theplugin.idea.ui.tree;

import javax.swing.tree.DefaultTreeModel;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 11, 2008
 * Time: 1:31:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class AtlassianTreeModel extends DefaultTreeModel {

	public AtlassianTreeModel(AtlassianTreeNode root) {
		super(root);
	}
	
}
