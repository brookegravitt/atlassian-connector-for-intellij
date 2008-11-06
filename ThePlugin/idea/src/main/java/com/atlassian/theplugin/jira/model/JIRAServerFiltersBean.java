package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * User: pmaruszak
 */
public class JIRAServerFiltersBean {
	private List<JIRAQueryFragment> manualFilter = new ArrayList<JIRAQueryFragment>();
	private List<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>();

	public List<JIRAQueryFragment> getManualFilter() {
		return manualFilter;
	}

	public void setManualFilter(final List<JIRAQueryFragment> manualFilter) {
		this.manualFilter = manualFilter;
	}

	public List<JIRASavedFilter> getSavedFilters() {
		return savedFilters;
	}

	public void setSavedFilters(final List<JIRASavedFilter> savedFilter) {
		this.savedFilters = savedFilter;
	}



}
