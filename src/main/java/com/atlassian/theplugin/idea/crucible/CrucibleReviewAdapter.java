package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.crucible.api.ReviewData;
import com.atlassian.theplugin.crucible.api.PermId;
import com.atlassian.theplugin.crucible.api.State;


public class CrucibleReviewAdapter {
	private ReviewData review;

	public CrucibleReviewAdapter(ReviewData review) {
		this.review = review;
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
