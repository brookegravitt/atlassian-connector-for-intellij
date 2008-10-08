package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.commons.crucible.api.model.Review;

public class ReviewKeyCellRenderer extends ReviewCellRenderer {
	protected String getCellText(Review review) {
		return review.getPermId().getId();
	}

	protected String getCellToolTipText(Review review) {
		return null;
	}
}
