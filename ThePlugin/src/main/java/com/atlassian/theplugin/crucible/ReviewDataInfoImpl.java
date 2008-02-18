package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.crucible.api.ReviewData;
import com.atlassian.theplugin.crucible.api.PermId;
import com.atlassian.theplugin.crucible.api.State;

import java.util.List;


class ReviewDataInfoImpl implements ReviewDataInfo {
	private final ReviewData reviewData;
	private final List<String> reviewers;
	private final Server server;

	public ReviewDataInfoImpl(ReviewData reviewData, List<String> reviewers, Server server) {
		this.reviewData = reviewData;
		this.reviewers = reviewers;
		this.server = server;
	}

	public String getReviewUrl() {
		String baseUrl = server.getUrlString();
		while (baseUrl.charAt(baseUrl.length() - 1) == '/') {
			// quite ineffective, I know ...
			baseUrl = baseUrl.substring(0,  baseUrl.length() - 1);
		}
		return baseUrl + "/cru/" + reviewData.getPermaId().getId();

	}

	public List<String> getReviewers() {
		return reviewers;
	}

	public Server getServer() {
		return server;
	}


	public String getAuthor() {
		return reviewData.getAuthor();
	}

	public String getCreator() {
		return reviewData.getCreator();
	}

	public String getDescription() {
		return reviewData.getDescription();
	}

	public String getModerator() {
		return reviewData.getModerator();
	}

	public String getName() {
		return reviewData.getName();
	}

	public PermId getParentReview() {
		return reviewData.getParentReview();
	}

	public PermId getPermaId() {
		return reviewData.getPermaId();
	}

	public String getProjectKey() {
		return reviewData.getProjectKey();
	}

	public String getRepoName() {
		return reviewData.getRepoName();
	}

	public State getState() {
		return reviewData.getState();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReviewDataInfoImpl that = (ReviewDataInfoImpl) o;

		if (!reviewData.getPermaId().getId().equals(that.reviewData.getPermaId().getId())) {
			return false;
		}
		if (server != null ? !server.equals(that.server) : that.server != null) {
			return false;
		}

		return true;
	}

	private static final int ONE_EFF = 31;

	public int hashCode() {
		int result;
		result = reviewData.getPermaId().getId().hashCode();
		result = ONE_EFF * result + (server != null ? server.hashCode() : 0);
		return result;
	}

}
