package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.CrucibleStatementOfObjectivesNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralSectionNode;

public class CrucibleFilesNode extends CrucibleContainerNode {

	public CrucibleFilesNode(Review review) {
		super(review);
	}

	protected String getText() {
		return "Reviewed Files";
	}

	public AtlassianTreeNode getClone() {
		return new CrucibleFilesNode(getReview());
	}

	public int compareTo(Object o) {
		if (o instanceof CrucibleFilesNode) {
			return 0;
		} else if (o instanceof CrucibleStatementOfObjectivesNode || o instanceof GeneralSectionNode) {
			return -1;
		}
		return 1;
	}
}
