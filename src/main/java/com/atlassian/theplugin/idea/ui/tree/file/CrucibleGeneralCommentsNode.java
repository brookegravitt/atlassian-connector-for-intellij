package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class CrucibleGeneralCommentsNode extends CrucibleContainerNode {
	private CrucibleFileInfo file;

	public CrucibleGeneralCommentsNode(ReviewData review, CrucibleFileInfo file, Map<String, FileNode> children) {
		super(review);
		this.file = file;

		if (children != null) {
			for (FileNode n : children.values()) {
				addNode(n);
			}
		} else {
			try {
				if (file == null) {
					List<GeneralComment> comments = review.getGeneralComments();
					for (GeneralComment c : comments) {
						if (!c.isDeleted()) {
							GeneralCommentTreeNode commentNode = new GeneralCommentTreeNode(review, c, null);
							addNode(commentNode);

							for (GeneralComment reply : c.getReplies()) {
								commentNode.addNode(new GeneralCommentTreeNode(review, reply, null));
							}
						}
					}
				} else {
					List<VersionedComment> comments = getFileVersionedComments();
					for (VersionedComment c : comments) {
						if (!c.isDeleted()) {
							VersionedCommentTreeNode commentNode = new VersionedCommentTreeNode(review, file, c, null);
							addNode(commentNode);

							for (VersionedComment reply : c.getReplies()) {
								commentNode.addNode(new VersionedCommentTreeNode(review, file, reply, null));
							}
						}
					}
				}
			} catch (ValueNotYetInitialized e) {
				// now what?
			}
		}
	}

	protected String getText() {
		String txt = file != null ? "Revision Comments" : "General Comments";
		txt += " (" + getNumberOfGeneralComments() + ")";
		return txt;
	}

	public AtlassianTreeNode getClone() {
		return new CrucibleGeneralCommentsNode(getReview(), file, getChildren());
	}

	private int getNumberOfGeneralComments() {
		int n = 0;

		try {
			if (file == null) {
				n = getReview().getGeneralComments().size();
				for (GeneralComment gc : getReview().getGeneralComments()) {
					n += gc.getReplies().size();
				}
			} else {
				List<VersionedComment> comments = getFileVersionedComments();
				for (VersionedComment c : comments) {
					++n;
					n += c.getReplies().size();
				}
			}
		} catch (ValueNotYetInitialized e) {
			return 0;
		}
		return n;
	}

	private List<VersionedComment> getFileVersionedComments() {
		List<VersionedComment> list = new ArrayList<VersionedComment>();
		try {
			List<VersionedComment> thisFileComments = file.getVersionedComments();

			for (VersionedComment c : thisFileComments) {
				if (c.getFromStartLine() + c.getFromEndLine() + c.getToStartLine() + c.getToEndLine() == 0) {
					if (!c.isReply()) {
						list.add(c);
					}
				}
			}
		} catch (ValueNotYetInitialized e) {
			return null;
		}
		return list;
	}

	public boolean isCompactable() {
		return false;
	}
}
