package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.atlassian.theplugin.configuration.Server;

import java.util.List;

/**
 * A simple POJO to represent all elements of a remote review. 
 */
public class RemoteReview {
    private ReviewData reviewData;
    private List<String> reviewers;
    private Server server;

	public RemoteReview(ReviewData reviewData, List<String> reviewers, Server server) {
		this.reviewData = reviewData;
		this.reviewers = reviewers;
		this.server = server;
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

	public Server getServer() {
		return server;
	}

	public String getReviewUrl() {
        return server.getUrlString() + "/cru/" + reviewData.getPermaId().getId();
    }

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		RemoteReview that = (RemoteReview) o;

		if (!reviewData.getPermaId().getId().equals(that.reviewData.getPermaId().getId())) {
			return false;
		}
		if (server != null ? !server.equals(that.server) : that.server != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = reviewData.getPermaId().getId().hashCode();
		result = 31 * result + (server != null ? server.hashCode() : 0);
		return result;
	}
}
