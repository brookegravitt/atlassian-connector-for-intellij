package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-07-14
 * Time: 14:03:40
 * To change this template use File | Settings | File Templates.
 */
public class ReviewAuthorDecorator {
	private String text;

	public ReviewAuthorDecorator(String value, ReviewDataInfoAdapter review) {
		text = value;

		String me = review.getServer().getUserName();

		if (review.getAuthor().getUserName().equals(me) || review.getModerator().getUserName().equals(me)) {
			text = "<html><body><span style=\"color: #ff0000; \">"
						+ text
						+ "</span></body></html>";
		}
	}

	public String toString() {
		return text;
	}

}