package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

import java.util.*;

/**
 * User: jgorycki
 * Date: Dec 16, 2008
 * Time: 1:53:24 PM
 */
public abstract class AbstractSortingCrucibleReviewListModel extends CrucibleReviewListModelListenerHolder {
	public AbstractSortingCrucibleReviewListModel(CrucibleReviewListModel parent) {
		super(parent);
	}

	protected Collection<ReviewAdapter> sort(Collection<ReviewAdapter> col) {
		List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
		for (ReviewAdapter r : col) {
			list.add(r);
		}
		Collections.sort(list, getComparator());
		return list;
	}

	protected abstract Comparator<ReviewAdapter> getComparator();

	public Collection<ReviewAdapter> getReviews() {
		return sort(parent.getReviews());
	}
}
