package com.atlassian.theplugin.idea.crucible;

import java.util.List;

public class ReviewNotificationBean {
	private List<ReviewData> reviews;
	private Exception exception;

	public ReviewNotificationBean() {
	}

	public List<ReviewData> getReviews() {
		return reviews;
	}

	public void setReviews(final List<ReviewData> reviews) {
		this.reviews = reviews;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(final Exception exception) {
		this.exception = exception;
	}
}
