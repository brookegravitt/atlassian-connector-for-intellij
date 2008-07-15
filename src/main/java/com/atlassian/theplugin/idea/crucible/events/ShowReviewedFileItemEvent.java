package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jun 17, 2008
* Time: 8:42:22 PM
* To change this template use File | Settings | File Templates.
*/
public class ShowReviewedFileItemEvent extends CrucibleEvent {
	private ReviewData reviewInfo;
	private CrucibleFileInfo reviewItem;

	public ShowReviewedFileItemEvent(CrucibleReviewActionListener caller, ReviewData reviewInfo,
            CrucibleFileInfo reviewItem) {
		super(caller);
		this.reviewInfo = reviewInfo;
		this.reviewItem = reviewItem;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.showReviewedFileItem(reviewInfo, reviewItem);
	}
}
