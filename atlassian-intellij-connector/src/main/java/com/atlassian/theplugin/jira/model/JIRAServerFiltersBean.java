package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRASavedFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author pmaruszak
 */
public class JIRAServerFiltersBean {
	private Set<JiraCustomFilter> manualFilters = new HashSet<JiraCustomFilter>();
	private List<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>();


    public Set<JiraCustomFilter> getManualFilters() {
        return manualFilters;
    }

    public void setManualFilters(Set<JiraCustomFilter> manualFilters) {
        this.manualFilters = manualFilters;
    }


	public List<JIRASavedFilter> getSavedFilters() {
		return savedFilters;
	}

	public void setSavedFilters(final List<JIRASavedFilter> savedFilter) {
		this.savedFilters = savedFilter;
	}



}
