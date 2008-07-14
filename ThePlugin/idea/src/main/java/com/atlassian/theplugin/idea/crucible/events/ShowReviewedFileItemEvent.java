package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jun 17, 2008
* Time: 8:42:22 PM
* To change this template use File | Settings | File Templates.
*/
public class ShowReviewedFileItemEvent extends CrucibleEvent {
	private ReviewDataInfoAdapter reviewInfo;
	private ReviewItem reviewItem;

	public ShowReviewedFileItemEvent(CrucibleReviewActionListener caller, ReviewDataInfoAdapter reviewInfo,
            ReviewItem reviewItem) {
		super(caller);
		this.reviewInfo = reviewInfo;
		this.reviewItem = reviewItem;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showReviewedFileItem(reviewInfo, reviewItem);
	}
}
