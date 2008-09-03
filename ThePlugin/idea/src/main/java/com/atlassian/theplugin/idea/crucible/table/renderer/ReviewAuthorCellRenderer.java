package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewData;

public class ReviewAuthorCellRenderer extends ReviewCellRenderer {
	protected String getCellText(ReviewData review) {
		return review.getAuthor().getDisplayName();
	}

	protected String getCellToolTipText(ReviewData review) {
		return null;
	}
}

