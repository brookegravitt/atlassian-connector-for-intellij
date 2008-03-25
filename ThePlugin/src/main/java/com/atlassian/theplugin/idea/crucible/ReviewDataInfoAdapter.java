package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.crucible.ReviewDataInfo;
import com.atlassian.theplugin.crucible.api.PermId;
import com.atlassian.theplugin.crucible.api.State;

import java.util.List;


public class ReviewDataInfoAdapter {
	private ReviewDataInfo review;

	public ReviewDataInfoAdapter(ReviewDataInfo review) {
		this.review = review;
	}

	public String getReviewUrl() {
		return review.getReviewUrl();
	}

	public String getAuthor() {
		return review.getAuthor();
	}

	public String getCreator() {
		return review.getCreator();
	}

	public String getDescription() {
		return review.getDescription();
	}

	public String getModerator() {
		return review.getModerator();
	}

	public List<String> getReviewers() {
		return review.getReviewers();
	}

	public String getName() {
		return review.getName();
	}

	public PermId getParentReview() {
		return review.getParentReview();
	}

	public PermId getPermaId() {
		return review.getPermaId();
	}

	public String getProjectKey() {
		return review.getProjectKey();
	}

	public String getRepoName() {
		return review.getRepoName();
	}

	public State getState() {
		return review.getState();
	}
}
