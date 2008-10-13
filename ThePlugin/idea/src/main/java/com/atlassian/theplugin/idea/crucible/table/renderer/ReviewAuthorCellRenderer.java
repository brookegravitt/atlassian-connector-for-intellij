package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;

public class ReviewAuthorCellRenderer extends ReviewCellRenderer {
	protected String getCellText(ReviewDataImpl review) {
		return review.getAuthor().getDisplayName();
	}

	protected String getCellToolTipText(ReviewDataImpl review) {
		return null;
	}
}

