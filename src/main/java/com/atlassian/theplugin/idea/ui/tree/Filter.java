package com.atlassian.theplugin.idea.ui.tree;

/**
 * Created by IntelliJ IDEA.
* User: pmaruszak
* Date: Aug 6, 2008
* Time: 11:42:11 AM
* To change this template use File | Settings | File Templates.
*/
public abstract class Filter {
	public abstract boolean isValid(AtlassianTreeNode node);
	

	public static final Filter ALL = new Filter() {
		public boolean isValid(final AtlassianTreeNode node) {
			return true;
		}
	};

//	public boolean acceptCrucibleNode(CrucibleFileNode crucibleFileNode);
}
