package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Aug 4, 2008
 * Time: 4:24:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnFileEvent extends CrucibleEvent {
	private ReviewData review;
	private CrucibleFileInfo file;

	public FocusOnFileEvent(final CrucibleReviewActionListener caller,
			final ReviewData review, final CrucibleFileInfo file) {
		super(caller);

		this.review = review;
		this.file = file;
	}

	protected void notify(final CrucibleReviewActionListener listener) {
		listener.focusOnFile(review, file);
	}
}
