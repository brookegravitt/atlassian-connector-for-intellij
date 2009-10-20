package com.atlassian.theplugin.idea.action.crucible.comment.gutter;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.intellij.openapi.actionSystem.AnAction;

public abstract class AbstractGutterCommentAction extends AnAction {
	protected ReviewAdapter review;
	protected VersionedComment comment;
	protected CrucibleFileInfo file;

	public void setComment(final VersionedComment comment) {
		this.comment = comment;
	}

	public void setReview(final ReviewAdapter review) {
		this.review = review;
	}

	public void setFile(final CrucibleFileInfo file) {
		this.file = file;
	}
}
