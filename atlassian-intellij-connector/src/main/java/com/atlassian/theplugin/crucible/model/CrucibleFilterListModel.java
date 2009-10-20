package com.atlassian.theplugin.crucible.model;

import com.atlassian.connector.intellij.crucible.RecentlyOpenReviewsFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: pmaruszak
 */
public class CrucibleFilterListModel {
	private Collection<PredefinedFilter> predefinedFilters = new ArrayList<PredefinedFilter>();
	private CustomFilter customFilter;
	private RecentlyOpenReviewsFilter recentlyOpenFilter;

	public CrucibleFilterListModel(CustomFilter customFilter, RecentlyOpenReviewsFilter recentlyOpenFilter) {
		for (PredefinedFilter predefinedFilter : PredefinedFilter.values()) {
			if (predefinedFilter.isRemote()) {
				predefinedFilters.add(predefinedFilter);
			}
		}
		this.customFilter = customFilter;
		this.recentlyOpenFilter = recentlyOpenFilter;
	}


	public Collection<PredefinedFilter> getPredefinedFilters() {
		return predefinedFilters;
	}

	public CustomFilter getCustomFilter() {
		return customFilter;
	}

	public RecentlyOpenReviewsFilter getRecentlyOpenReviewsFilter() {
		return recentlyOpenFilter;
	}
}
