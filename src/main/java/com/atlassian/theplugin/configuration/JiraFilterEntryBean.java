package com.atlassian.theplugin.configuration;

import java.util.HashMap;
import java.util.Map;

public class JiraFilterEntryBean {
	private Map<String, String> filterEntry = new HashMap<String, String>();

	public JiraFilterEntryBean() {
	}

	public JiraFilterEntryBean(Map<String, String> filterEntry) {
		this.filterEntry = filterEntry;
	}

	public Map<String, String> getFilterEntry() {
		return filterEntry;
	}

	public void setFilterEntry(Map<String, String> filterEntry) {
		this.filterEntry = filterEntry;
	}
}