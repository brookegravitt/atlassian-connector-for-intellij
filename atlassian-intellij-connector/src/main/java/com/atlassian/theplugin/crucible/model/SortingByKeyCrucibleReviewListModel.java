package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.table.column.ReviewKeyComparator;

import java.util.Collections;
import java.util.Comparator;

/**
 * User: jgorycki
 * Date: Dec 16, 2008
 * Time: 2:03:27 PM
 */
public class SortingByKeyCrucibleReviewListModel extends AbstractSortingCrucibleReviewListModel {
	private static final Comparator<ReviewAdapter> COMPARATOR = Collections.reverseOrder(new ReviewKeyComparator());

	public SortingByKeyCrucibleReviewListModel(CrucibleReviewListModel parent) {
		super(parent);
	}

	protected Comparator<ReviewAdapter> getComparator() {
		return COMPARATOR;
	}
}
