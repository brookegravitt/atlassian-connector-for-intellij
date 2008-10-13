package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;

public class ReviewStateCellRenderer extends ReviewCellRenderer {
	protected String getCellText(ReviewDataImpl review) {
		return review.getState().value();
	}

	protected String getCellToolTipText(ReviewDataImpl review) {
		return null;
	}
}

