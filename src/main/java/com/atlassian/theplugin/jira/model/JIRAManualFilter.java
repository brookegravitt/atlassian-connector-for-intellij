package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAQueryFragment;

import java.util.List;

/**
 * User: pmaruszak
 */
public final class JIRAManualFilter {
	List<JIRAQueryFragment> queryFragment;
	String name;

	JIRAManualFilter(final String name, List<JIRAQueryFragment> queryFragment){
		this.name = name;
		this.queryFragment = queryFragment;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<JIRAQueryFragment> getQueryFragment() {
		return queryFragment;
	}

	public void setQueryFragment(final List<JIRAQueryFragment> queryFragment) {
		this.queryFragment = queryFragment;
	}
}
