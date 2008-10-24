package com.atlassian.theplugin.idea.crucible.table.renderer;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

public class ReviewKeyCellRenderer extends ReviewCellRenderer {
	protected String getCellText(ReviewAdapter review) {
		return review.getPermId().getId();
	}

	protected String getCellToolTipText(ReviewAdapter review) {
		return null;
	}
}
