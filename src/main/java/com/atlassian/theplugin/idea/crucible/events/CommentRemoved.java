package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.CrucibleBottomToolWindowPanel;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 29, 2008
 * Time: 3:45:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentRemoved extends CrucibleEvent {
    private ReviewData review;
    private Comment comment;

    public CommentRemoved(final CrucibleReviewActionListener caller, final ReviewData review, final Comment comment) {
        super(caller);
        this.review = review;
        this.comment = comment;
    }

    protected void notify(final CrucibleReviewActionListener listener) {
        listener.removedComment(review, comment);
    }
}
