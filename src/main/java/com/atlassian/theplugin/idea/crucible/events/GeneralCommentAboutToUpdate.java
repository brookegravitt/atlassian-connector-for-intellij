package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 29, 2008
 * Time: 2:55:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralCommentAboutToUpdate extends CrucibleEvent {
    private ReviewData review;
    private GeneralComment comment;

    public GeneralCommentAboutToUpdate(final CrucibleReviewActionListener caller, final ReviewData review,
            final GeneralComment comment) {
        super(caller);
        this.review = review;
        this.comment = comment;
    }

    protected void notify(final CrucibleReviewActionListener listener) {
        listener.aboutToUpdateGeneralComment(review, comment);
    }
}
