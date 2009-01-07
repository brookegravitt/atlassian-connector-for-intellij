package com.atlassian.theplugin.crucible.model;

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

	public CrucibleFilterListModel(CustomFilter customFilter) {
		for (PredefinedFilter predefinedFilter : PredefinedFilter.values()) {
			if (predefinedFilter.isRemote()) {
				predefinedFilters.add(predefinedFilter);
			}
		}
		this.customFilter = customFilter;
	}


	public Collection<PredefinedFilter> getPredefinedFilters() {
		return predefinedFilters;
	}

	public CustomFilter getCustomFilter() {
		return customFilter;
	}
}
