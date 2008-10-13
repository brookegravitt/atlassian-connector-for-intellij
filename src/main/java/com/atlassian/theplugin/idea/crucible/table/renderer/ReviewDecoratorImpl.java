/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;
import com.atlassian.theplugin.util.ColorToHtml;
import com.intellij.util.ui.UIUtil;

import java.util.List;

public class ReviewDecoratorImpl implements ReviewDecorator {
	private String text;
	private ReviewAdapter review;
	private boolean isSelected;

	public ReviewDecoratorImpl(String value, ReviewAdapter review, boolean isSelected) {
		this.text = value;
		this.review = review;
		this.isSelected = isSelected;

		// first decorator is most important (takes precedence)
		selectionDecorator();
		stateDecorator();
		reviewerFinishedDecorator();
		authorModeratorDecorator();

		// this should be the last decorator
		htmlDecorator();
	}


	public String getString() {
		return text;
	}

	/**
	 * Decorates text if current user is author or moderator of the review
	 */
	private void authorModeratorDecorator() {

		String me = review.getServer().getUsername();

		if (review.getAuthor().getUserName().equals(me) || review.getModerator().getUserName().equals(me)) {
			text = "<span style=\"color: #009900; \">"
					+ text
					+ "</span>";
		}
	}


	/**
	 * Decorates text if current user is reviewer of the review and did (or not) review already
	 */
	private void reviewerFinishedDecorator() {

		String me = review.getServer().getUsername();

		try {
			Reviewer reviewer = findReviewer(review.getReviewers(), me);

			if (reviewer != null && reviewer.isCompleted()) {
				text = "<span style=\"color: #999999; \">"
							+ text
							+ "</span>";
			} else if (reviewer != null && !reviewer.isCompleted()) {
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
	private void stateDecorator() {

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

	private void selectionDecorator() {
		if (isSelected) {
			text = "<span style=\"color: "
					+ ColorToHtml.getHtmlFromColor(UIUtil.getListSelectionForeground())
					+ "\">"
					+ text
					+ "</span>";
		}
	}

	/**
	 * Decorates text with <html><body>...</body></html> tags
	 */
	private void htmlDecorator() {
		text = "<html><body>" + text + "</body></html>";
	}

}


