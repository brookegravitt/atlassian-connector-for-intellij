package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: jgorycki
 * Date: Dec 15, 2008
 * Time: 4:05:54 PM
 */
public class SearchingCrucibleReviewListModel 
		extends CrucibleReviewListModelListenerHolder implements CrucibleReviewListModel {


	private String searchTerm;
	private CrucibleReviewListModel parent;

	public SearchingCrucibleReviewListModel(CrucibleReviewListModel parent) {
		this.parent = parent;
		parent.addListener(this);
		searchTerm = "";
	}

	public void setSearchTerm(@NotNull String searchTerm) {

		if (this.searchTerm.equals(searchTerm)) {
			return;
		}
		this.searchTerm = searchTerm.toLowerCase();

		modelChanged();
	}

	private Collection<ReviewAdapter> search(Collection<ReviewAdapter> col) {
		if (searchTerm.length() == 0) {
			return col;
		}
		List<ReviewAdapter> list = new ArrayList<ReviewAdapter>();
		for (ReviewAdapter r : col) {
			if (r.getPermId().getId().toLowerCase().indexOf(searchTerm) > -1
					|| r.getName().toLowerCase().indexOf(searchTerm) > -1) {
				list.add(r);
			}
		}
		return list;
	}

	public Collection<ReviewAdapter> getReviews() {
		return search(parent.getReviews());
	}

	public void addReview(ReviewAdapter review) {
		parent.addReview(review);
	}

	public void removeReview(ReviewAdapter review) {
		parent.removeReview(review);
	}

	public void removeAll() {
		parent.removeAll();
	}

	public void updateReviews(CrucibleServerCfg serverCfg, Collection<ReviewAdapter> updatedReviews) {
		parent.updateReviews(serverCfg, updatedReviews);
	}

	public ReviewAdapter getSelectedReview() {
		return parent.getSelectedReview();
	}

	public void setSelectedReview(ReviewAdapter review) {
		parent.setSelectedReview(review);
	}
}
