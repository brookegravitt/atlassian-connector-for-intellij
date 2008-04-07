package com.atlassian.theplugin.configuration;

import java.util.ArrayList;
import java.util.List;

public class JiraFiltersBean {
	private List<JiraFilterEntryBean> manualFilter = new ArrayList<JiraFilterEntryBean>();
	private JiraFilterEntryBean savedFilter = null;
	private boolean savedFilterUsed = false;

	public JiraFiltersBean() {
	}

	public List<JiraFilterEntryBean> getManualFilter() {
		return manualFilter;
	}

	public void setManualFilter(List<JiraFilterEntryBean> manualFilter) {
		this.manualFilter = manualFilter;
	}

	public JiraFilterEntryBean getSavedFilter() {
		return savedFilter;
	}

	public void setSavedFilter(JiraFilterEntryBean savedFilter) {
		this.savedFilter = savedFilter;
	}

	public boolean getSavedFilterUsed() {
		return savedFilterUsed;
	}

	public void setSavedFilterUsed(boolean savedFilterUsed) {
		this.savedFilterUsed = savedFilterUsed;
	}
}
