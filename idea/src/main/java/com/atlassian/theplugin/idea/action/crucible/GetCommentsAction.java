package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.ShowReviewEvent;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import java.util.Collection;


public class GetCommentsAction extends TableSelectedAction implements CrucibleReviewActionListener {

	public void focusOnReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void showReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
	}

	public void showReviewedFileItem(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
	}

	public void showGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
	}

	public void showGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
	}

	public void showVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
	}

	public void showVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
	}

	protected void itemSelected(Object row) {
		IdeaHelper.getReviewActionEventBroker().trigger(new ShowReviewEvent(this, (ReviewDataInfoAdapter) row));
	}
}
