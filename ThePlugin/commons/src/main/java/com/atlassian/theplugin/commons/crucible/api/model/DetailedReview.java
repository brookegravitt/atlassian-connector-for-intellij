package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.List;

public interface DetailedReview extends Review {
    List<Reviewer> getReviewers();

    List<ReviewItem> getReviewItems();

    List<GeneralComment> getGeneralComments();

    List<VersionedComment> getVersionedComments();
    
    List<Transition> getTransitions();
}
