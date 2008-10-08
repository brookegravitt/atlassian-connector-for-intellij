package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.commons.crucible.api.model.Review;

public class ReviewAuthorCellRenderer extends ReviewCellRenderer {
	protected String getCellText(Review review) {
		return review.getAuthor().getDisplayName();
	}

	protected String getCellToolTipText(Review review) {
		return null;
	}
}

