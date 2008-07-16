package com.atlassian.theplugin.idea.crucible.tree;


import com.atlassian.theplugin.idea.crucible.ReviewData;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 12, 2008
 * Time: 12:22:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleTreeRootNode extends DefaultMutableTreeNode {
	static final long serialVersionUID = 0L;

	private ReviewData changeSet;

	CrucibleTreeRootNode(ReviewData infoAdapater) {
		changeSet = infoAdapater;
	}

	CrucibleTreeRootNode() {
		
	}

	public String toString() {
		if (changeSet != null) {
			return changeSet.toString();
		} else {
			return "No Review is selected";
		}
	}



	public ReviewData getCrucibleChangeSet() {
		return changeSet;
	}

	public void setCrucibleChangeSet(ReviewData changeSet) {
		this.changeSet = changeSet;
	}
}
