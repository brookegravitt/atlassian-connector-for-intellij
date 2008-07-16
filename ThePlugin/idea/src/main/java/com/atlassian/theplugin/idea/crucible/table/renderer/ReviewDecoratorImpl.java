package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;

import java.util.List;

public class ReviewDecoratorImpl implements ReviewDecorator {
	private String text;
	private ReviewData review;

	public ReviewDecoratorImpl(String value, ReviewData review) {
		this.text = value;
		this.review = review;

		// first decorator is most important (takes precedence)
		StateDecorator();
		ReviewerFinishedDecorator();
		AuthorModeratorDecorator();

		// this should be the last decorator
		HtmlDecorator();
	}


	public String getString() {
		return text;
	}

	/**
	 * Decorates text if current user is author or moderator of the review
	 */
	private void AuthorModeratorDecorator() {

		String me = review.getServer().getUserName();

		if (review.getAuthor().getUserName().equals(me) || review.getModerator().getUserName().equals(me)) {
			text = "<span style=\"color: #009900; \">"
					+ text
					+ "</span>";
		}
	}


	/**
	 * Decorates text if current user is reviewer of the review and did (or not) review already
	 */
	private void ReviewerFinishedDecorator() {

		String me = review.getServer().getUserName();

		try {
			Reviewer reviewer = findReviewer(review.getReviewers(), me);

			if (reviewer != null && reviewer.isCompleted()) {
				text = "<span style=\"color: #999999; \">"
							+ text
							+ "</span>";
			} else if (reviewer != null && ! reviewer.isCompleted()) {
				text = "<span style=\"color: #0000aa; \">"
							+ text
							+ "</span>";
			}
			
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// blame Lukasz
			valueNotYetInitialized.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	/**
	 *
	 * @param reviewers list of reviewers
	 * @param me current user userName
	 * @return reviewer reference if user me is one of reviewers or null otherwise
	 */
	private Reviewer findReviewer(List<Reviewer> reviewers, String me) {
		for (Reviewer reviewer : reviewers) {
			if (reviewer.getUserName().equals(me)) {
				return reviewer;
			}
		}
		return null;
	}

	/**
	 * Decorates text if review has been closed
	 */
	private void StateDecorator() {

		switch (review.getState()) {
			case CLOSED:
			case DEAD:
				text = "<span style=\"color: #999999; text-decoration: line-through;\">"
						+ text
						+ "</span>";
				break;
			default:
		}
	}


	/**
	 * Decorates text with <html><body>...</body></html> tags
	 */
	private void HtmlDecorator() {
		text = "<html><body>" + text + "</body></html>";
	}

}


