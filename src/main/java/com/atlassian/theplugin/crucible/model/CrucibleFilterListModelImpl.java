package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: pmaruszak
 */
public class CrucibleFilterListModelImpl implements CrucibleFilterListModel {
	private Collection<CrucibleFilterListModelListener> listeners = new ArrayList<CrucibleFilterListModelListener>();
	private Collection<PredefinedFilter> predefinedFilters = new ArrayList<PredefinedFilter>();
	private Collection<CustomFilter> customFilters = new ArrayList<CustomFilter>();
	private PredefinedFilter selectedPredefinedFilter;
	private CustomFilter selectedCustomFilter;

	public CrucibleFilterListModelImpl() {
		predefinedFilters.add(PredefinedFilter.Abandoned);
		predefinedFilters.add(PredefinedFilter.Closed);
		predefinedFilters.add(PredefinedFilter.Drafts);
		predefinedFilters.add(PredefinedFilter.Open);
		predefinedFilters.add(PredefinedFilter.OutForReview);
		predefinedFilters.add(PredefinedFilter.RequireMyApproval);
		predefinedFilters.add(PredefinedFilter.ToReview);
		predefinedFilters.add(PredefinedFilter.ToSummarize);		
	}

	public void setSelectedCustomFilter(CustomFilter filter) {
		selectedCustomFilter = filter;
		fireSelectedCustomFilter();
	}

	private void fireSelectedCustomFilter() {
		for (CrucibleFilterListModelListener listener : listeners) {
			listener.selectedCutomFilter();
		}
	}

	public CustomFilter getSelectedCustomFilter() {
		return selectedCustomFilter;
	}

	public void setSelectedPredefinedFilter(PredefinedFilter filter) {
		selectedPredefinedFilter = filter;
		fireSelectedPredefinedFilter();
	}

	private void fireSelectedPredefinedFilter() {
		for (CrucibleFilterListModelListener listener : listeners) {
			listener.selectedCutomFilter();
		}
	}

	public PredefinedFilter getSelectedPredefinedFilter() {
		return selectedPredefinedFilter;
	}

	public Collection<PredefinedFilter> getPredefinedFilters() {
		return predefinedFilters;
	}

	public Collection<CustomFilter> getCustomFilters() {
		return customFilters;
	}

	public void addListener(CrucibleFilterListModelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(CrucibleFilterListModelListener listener) {
		listeners.remove(listener);
	}
}
