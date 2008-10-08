package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.commons.crucible.api.model.Review;

public class ReviewStateCellRenderer extends ReviewCellRenderer {
	protected String getCellText(Review review) {
		return review.getState().value();
	}

	protected String getCellToolTipText(Review review) {
		return null;
	}
}

