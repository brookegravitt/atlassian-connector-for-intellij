package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;

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

	private ReviewData reviewData;

	CrucibleTreeRootNode(ReviewData infoAdapater) {
		reviewData = infoAdapater;
	}

	CrucibleTreeRootNode() {
		
	}

	public String toString() {
		if (reviewData != null) {
			return reviewData.getProjectKey() + ", " + reviewData.getPermId()
                    + ", " + reviewData.getName();
		} else {
			return "No Review is selected";
		}
	}



	public ReviewData getCrucibleChangeSet() {
		return reviewData;
	}

	public void setCrucibleChangeSet(ReviewData reviewData) {
		this.reviewData = reviewData;
	}
}
