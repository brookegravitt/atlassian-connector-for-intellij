package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author pmaruszak
 */
public class JIRAServerFiltersBean {
	private HashMap<UUID, JiraCustomFilter> manualFilters = new HashMap<UUID, JiraCustomFilter>();
	private List<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>();


    public Collection<JiraCustomFilter> getManualFilters() {
        return manualFilters.values();
    }

    public void setManualFilters(Collection<JiraCustomFilter> manualFilters) {
        for (JiraCustomFilter filter : manualFilters) {
            this.manualFilters.put(filter.getUuid(), filter);
        }        
    }


	public List<JIRASavedFilter> getSavedFilters() {
		return savedFilters;
	}

	public void setSavedFilters(final List<JIRASavedFilter> savedFilter) {
		this.savedFilters = savedFilter;
	}



}
