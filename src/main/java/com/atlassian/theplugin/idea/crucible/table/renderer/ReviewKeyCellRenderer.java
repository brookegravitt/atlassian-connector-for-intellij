package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;

public class ReviewKeyCellRenderer extends ReviewCellRenderer {
	protected String getCellText(ReviewDataImpl review) {
		return review.getPermId().getId();
	}

	protected String getCellToolTipText(ReviewDataImpl review) {
		return null;
	}
}
