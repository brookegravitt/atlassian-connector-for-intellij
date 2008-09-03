package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewData;

public class ReviewStateCellRenderer extends ReviewCellRenderer {
	protected String getCellText(ReviewData review) {
		return review.getState().value();
	}

	protected String getCellToolTipText(ReviewData review) {
		return null;
	}
}

