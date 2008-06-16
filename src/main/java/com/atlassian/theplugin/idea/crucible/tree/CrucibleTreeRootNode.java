package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

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

	private ReviewDataInfoAdapter reviewDataInfoAdapter;

	CrucibleTreeRootNode(ReviewDataInfoAdapter infoAdapater){
		reviewDataInfoAdapter = infoAdapater;
	}

	CrucibleTreeRootNode(){
		
	}

	public String toString() {
		if (reviewDataInfoAdapter != null) {
			return reviewDataInfoAdapter.getProjectKey() + ", " + reviewDataInfoAdapter.getPermaId() + ", " + reviewDataInfoAdapter.getName();
		} else {
			return "No Review is selected";
		}
	}



	public ReviewDataInfoAdapter getReviewDataInfoAdapter() {
		return reviewDataInfoAdapter;
	}

	public void setReviewDataInfoAdapter(ReviewDataInfoAdapter reviewDataInfoAdapter) {
		this.reviewDataInfoAdapter = reviewDataInfoAdapter;
	}
}
