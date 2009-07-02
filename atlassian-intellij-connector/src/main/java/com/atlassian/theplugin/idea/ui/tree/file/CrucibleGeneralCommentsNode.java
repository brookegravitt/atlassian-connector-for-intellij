package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;

import java.util.List;
import java.util.Map;

public class CrucibleGeneralCommentsNode extends CrucibleContainerNode {
	public CrucibleGeneralCommentsNode(ReviewAdapter review, Map<String, FileNode> children) {
		super(review);

		if (children != null) {
			for (FileNode n : children.values()) {
				addNode(n);
			}
		} else {
			try {
				List<GeneralComment> comments = review.getGeneralComments();
				for (GeneralComment c : comments) {
					if (!c.isDeleted()) {
						GeneralCommentTreeNode commentNode = new GeneralCommentTreeNode(review, c, null);
						addNode(commentNode);

						for (Comment reply : c.getReplies()) {
							commentNode.addNode(new GeneralCommentTreeNode(review, (GeneralComment) reply, null));
						}
					}
				}
			} catch (ValueNotYetInitialized e) {
				// now what?
			}
		}
	}

	protected String getText() {
		return "General Comments (" + getNumberOfGeneralComments() + ")";
	}

	public AtlassianTreeNode getClone() {
		return new CrucibleGeneralCommentsNode(getReview(), getChildren());
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof CrucibleGeneralCommentsNode) {
			return 0;
		}
		return -1;
	}

	private int getNumberOfGeneralComments() {
		int n = 0;

		try {
			n = getReview().getGeneralComments().size();
			for (GeneralComment gc : getReview().getGeneralComments()) {
				n += gc.getReplies().size();
			}
		} catch (ValueNotYetInitialized e) {
			return 0;
		}
		return n;
	}

	@Override
	public boolean isCompactable() {
		return false;
	}
}
