package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.List;
import java.util.ArrayList;

public class DetailedReviewBean extends ReviewBean implements DetailedReview {
    private List<Reviewer> reviewers = new ArrayList<Reviewer>();
    private List<ReviewItem> reviewItems = new ArrayList<ReviewItem>();
    private List<GeneralComment> generalComments = new ArrayList<GeneralComment>();
    private List<VersionedComment> versionedComments = new ArrayList<VersionedComment>();
    private List<Transition> transitions = new ArrayList<Transition>();

    public List<Reviewer> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<Reviewer> reviewers) {
        this.reviewers = reviewers;
    }

    public List<ReviewItem> getReviewItems() {
        return reviewItems;
    }

    public void setReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
    }

    public List<GeneralComment> getGeneralComments() {
        return generalComments;
    }

    public void setGeneralComments(List<GeneralComment> generalComments) {
        this.generalComments = generalComments;
    }

    public List<VersionedComment> getVersionedComments() {
        return versionedComments;
    }

    public void setVersionedComments(List<VersionedComment> versionedComments) {
        this.versionedComments = versionedComments;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
    }
}
