package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 29, 2008
 * Time: 3:05:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralCommentUpdated extends CrucibleEvent {
    private ReviewData review;
    private GeneralComment comment;

    public GeneralCommentUpdated(final CrucibleReviewActionListener caller, final ReviewData review,
            final GeneralComment comment) {
        super(caller);
        this.review = review;
        this.comment = comment;
    }

    protected void notify(final CrucibleReviewActionListener listener) {
        listener.updatedGeneralComment(review, comment);
    }
}
