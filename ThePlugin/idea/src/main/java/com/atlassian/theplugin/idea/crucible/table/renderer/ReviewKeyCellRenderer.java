package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewData;

public class ReviewKeyCellRenderer extends ReviewCellRenderer {
	protected String getCellText(ReviewData review) {
		return review.getPermId().getId();
	}

	protected String getCellToolTipText(ReviewData review) {
		return null;
	}
}
