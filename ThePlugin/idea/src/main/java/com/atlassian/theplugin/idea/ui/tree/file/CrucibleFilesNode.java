package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;

public class CrucibleFilesNode extends CrucibleContainerNode {

	public CrucibleFilesNode(ReviewData review) {
		super(review);
	}

	protected String getText() {
		return "Reviewed Files";
	}

	public AtlassianTreeNode getClone() {
		return new CrucibleFilesNode(getReview());
	}

}
