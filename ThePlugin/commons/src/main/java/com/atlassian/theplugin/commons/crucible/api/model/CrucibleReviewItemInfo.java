package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.List;
import java.util.ArrayList;

public class CrucibleReviewItemInfo {

	private List<VersionedComment> comments;
	private PermId id;

	public CrucibleReviewItemInfo(PermId permId) {
		id = new PermIdBean(permId.getId());
		comments = new ArrayList<VersionedComment>();
	}

	public void addComment(VersionedComment c) {
		comments.add(c);
	}

	public List<VersionedComment> getComments() {
		return comments;
	}

	public void setComments(List<VersionedComment> comments) {
		this.comments = comments;
	}

	public PermId getId() {
		return id;
	}

	public int getNumberOfComments() {
		if (comments == null) {
			return 0;
		}
		int n = comments.size();
		for (VersionedComment c : comments) {
			n += c.getReplies().size();
		}
		return n;
	}

	public int getNumberOfDefects() {
		if (comments == null) {
			return 0;
		}
		int counter = 0;
		for (VersionedComment comment : comments) {
			if (comment.isDefectApproved()) {
				++counter;
			}
		}
		return counter;
	}
}
