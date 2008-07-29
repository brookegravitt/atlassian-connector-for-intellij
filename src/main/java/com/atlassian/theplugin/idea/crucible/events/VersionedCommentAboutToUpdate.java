package com.atlassian.theplugin.idea.crucible.events;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 29, 2008
 * Time: 2:48:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedCommentAboutToUpdate extends CrucibleEvent {
    private ReviewData review;
    private CrucibleFileInfo file;
    private VersionedComment comment;

    public VersionedCommentAboutToUpdate(final CrucibleReviewActionListener caller, final ReviewData review,
            final CrucibleFileInfo file, final VersionedComment comment) {
        super(caller);
        this.review = review;
        this.file = file;
        this.comment = comment;
    }

    protected void notify(final CrucibleReviewActionListener listener) {
        listener.aboutToUpdateVersionedComment(review, file, comment);
    }
}
