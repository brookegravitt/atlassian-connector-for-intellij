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

import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.util.ReviewInfoUtil;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.intellij.ui.RowIcon;
import com.intellij.openapi.util.IconLoader;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-07-14
 * Time: 08:54:39
 * To change this template use File | Settings | File Templates.
 */
public class ReviewReviewersCellRenderer  extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		JLabel label = (JLabel) c;

		//label.setHorizontalAlignment(SwingConstants.CENTER);

		if (value instanceof ReviewData) {

			ReviewData review = (ReviewData) value;

			try {
				if (review.getReviewers().size() > 5) {
					label.setText(createStringContent(review));
					label.setIcon(null);
				} else {
					label.setText("");
					label.setIcon(createIconContent(review));
				}
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				// ignore
			}

			label.setToolTipText(createHtmlTooltip(review));
		}
		return c;
	}

	private Icon createIconContent(ReviewData review) {
		int index = 0;
		RowIcon rowIcon = null;

		try {
			rowIcon = new RowIcon(review.getReviewers().size());
			for (Reviewer reviewer : review.getReviewers()) {
				if (reviewer.isCompleted()) {
					rowIcon.setIcon(IconLoader.getIcon("/icons/review_finished.gif"), index);
				} else {
					rowIcon.setIcon(IconLoader.getIcon("/icons/review_active.gif"), index);
				}
				index++;
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// ignore
		}

		return rowIcon;
	}

	private String createStringContent(ReviewData review) {

		int numReviews = 0;
		try {
			numReviews = review.getReviewers().size();
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// ignore
		}

		String text = ReviewInfoUtil.getNumOfCompletedReviewers(review) + "/" + numReviews;

		text = new ReviewDecoratorImpl(text, review).getString();

		return text;

	}

	private String createHtmlTooltip(ReviewData review) {

		StringBuffer html = new StringBuffer();

		html.append("<html><body>");

		try {
			for (Reviewer reviewer : review.getReviewers()) {
				if (reviewer.isCompleted()) {
					html.append("<span style=\"color: #999999; text-decoration: line-through; \">");
				} else {
					html.append("<span>");
				}
				html.append(reviewer.getDisplayName());
				html.append("</span>");
				html.append("<br>");
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// ignore
		}

		html.append("</body></html>");

		return html.toString();
	}
}
