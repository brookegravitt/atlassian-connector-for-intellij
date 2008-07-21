package com.atlassian.theplugin.idea.ui.tree;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 21, 2008
 * Time: 11:26:52 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AtlassianClickAction {
	public static final AtlassianClickAction EMPTY_ACTION = new AtlassianClickAction() {
		public void execute(AtlassianTreeNode node, int noOfClicks) {
			// do nothing
		}
	};

	void execute(AtlassianTreeNode node, int noOfClicks);
}
