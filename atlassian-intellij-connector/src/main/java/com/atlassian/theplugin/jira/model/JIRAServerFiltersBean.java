package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.JIRASavedFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * User: pmaruszak
 */
public class JIRAServerFiltersBean {
	private JIRAManualFilter manualFilter;
	private List<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>();

	public JIRAManualFilter getManualFilter() {
		return manualFilter;
	}

	public void setManualFilter(final JIRAManualFilter manualFilter) {
		this.manualFilter = manualFilter;
	}

	public List<JIRASavedFilter> getSavedFilters() {
		return savedFilters;
	}

	public void setSavedFilters(final List<JIRASavedFilter> savedFilter) {
		this.savedFilters = savedFilter;
	}



}
