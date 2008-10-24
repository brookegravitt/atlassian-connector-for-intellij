package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

public class ReviewAuthorCellRenderer extends ReviewCellRenderer {
	protected String getCellText(ReviewAdapter review) {
		return review.getAuthor().getDisplayName();
	}

	protected String getCellToolTipText(ReviewAdapter review) {
		return null;
	}
}

