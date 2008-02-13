package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;

import java.util.List;

/**
 * A simple POJO to represent all elements of a remote review. 
 */
public class RemoteReview {
    ReviewData reviewData;
    List<String> reviewers;
    String serverUrl;

    public RemoteReview(ReviewData reviewData, List<String> reviewers, String serverUrl) {
        this.reviewData = reviewData;
        this.reviewers = reviewers;
        this.serverUrl = serverUrl;
    }

    public ReviewData getReviewData() {
        return reviewData;
    }

    public void setReviewData(ReviewData reviewData) {
        this.reviewData = reviewData;
    }

    public List<String> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<String> reviewers) {
        this.reviewers = reviewers;
    }

    public String getReviewUrl() {
        return serverUrl + "/cru/" + reviewData.getPermaId().getId();
    }
}
