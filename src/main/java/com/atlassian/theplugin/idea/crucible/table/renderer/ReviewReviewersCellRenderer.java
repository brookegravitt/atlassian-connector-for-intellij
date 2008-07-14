package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;

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

		label.setHorizontalAlignment(SwingConstants.CENTER);

		if (value instanceof ReviewDataInfoAdapter) {

			ReviewDataInfoAdapter review = (ReviewDataInfoAdapter) value;


			label.setText(createStringContent(review));

			label.setToolTipText(createHtmlTooltip(review));
			label.setIcon(null);
		}
		return c;
	}

	private String createStringContent(ReviewDataInfoAdapter review) {

		int numReviews = review.getReviewers().size();

		String text = review.getNumOfCompletedReviewers() + "/" + numReviews;

		text = new ReviewAuthorDecorator(
				new ReviewStateDecorator(text, review.getState()).toString(),
				review).toString();

		return text;

	}

	private String createHtmlTooltip(ReviewDataInfoAdapter review) {

		StringBuffer html = new StringBuffer();

		html.append("<html><body>");

		for (Reviewer reviewer : review.getReviewers()) {
			if (reviewer.isCompleted()) {
				html.append("<span style=\"color: #999999; text-decoration: line-through; \">");
			} else {
				html.append("<span>");
			}
			html.append(reviewer.getUserName());
			html.append("</span>");
			html.append("<br />");
		}

		html.append("</body></html>");

		return html.toString();
	}
}
