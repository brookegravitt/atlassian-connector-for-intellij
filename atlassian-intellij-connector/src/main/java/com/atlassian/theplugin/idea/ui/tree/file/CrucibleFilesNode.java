package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralSectionNode;

public class CrucibleFilesNode extends CrucibleContainerNode {

	public CrucibleFilesNode(ReviewAdapter review) {
		super(review);
	}

	@Override
	protected String getText() {
		return "Reviewed Files";
	}

	@Override
	public AtlassianTreeNode getClone() {
		return new CrucibleFilesNode(getReview());
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof CrucibleFilesNode) {
			return 0;
		} else if (o instanceof GeneralSectionNode) {
			return -1;
		}
		return 1;
	}
}
