package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrucibleLineCommentsNode extends CrucibleContainerNode {

	private CrucibleFileInfo file;

	public CrucibleLineCommentsNode(ReviewAdapter review, CrucibleFileInfo file, Map<String, FileNode> children) {
		super(review);
		this.file = file;

		if (children != null) {
			for (FileNode n : children.values()) {
				addNode(n);
			}
		} else {
			List<VersionedComment> comments = getLineVersionedComments();
			for (VersionedComment c : comments) {
				if (!c.isDeleted()) {
					VersionedCommentTreeNode commentNode = new VersionedCommentTreeNode(review, file, c, null);
					addNode(commentNode);

					for (Comment reply : c.getReplies()) {
						commentNode.addNode(new VersionedCommentTreeNode(review, file, (VersionedComment) reply, null));
					}
				}
			}
		}
	}

	protected String getText() {
		return "Line Comments (" + getNumberOfLineComments() + ")";
	}

	public AtlassianTreeNode getClone() {
		return new CrucibleLineCommentsNode(getReview(), file, getChildren());
	}

	private int getNumberOfLineComments() {
		int n = 0;

		List<VersionedComment> thisFileComments = getLineVersionedComments();

		for (VersionedComment c : thisFileComments) {
			++n;
			n += c.getReplies().size();
		}

		return n;
	}

	private List<VersionedComment> getLineVersionedComments() {
		List<VersionedComment> list = new ArrayList<VersionedComment>();
		List<VersionedComment> thisFileComments = file.getVersionedComments();
		if (thisFileComments == null) {
			return null;
		}
		for (VersionedComment c : thisFileComments) {
			if (c.getFromStartLine() + c.getFromEndLine() + c.getToStartLine() + c.getToEndLine() != 0) {
				if (!c.isReply()) {
					list.add(c);
				}
			}
		}
		return list;
	}


	public boolean isCompactable() {
		return false;
	}
}

