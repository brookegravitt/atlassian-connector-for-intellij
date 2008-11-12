package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.JIRAQueryFragment;

import java.util.List;

/**
 * User: pmaruszak
 */
public final class JIRAManualFilter {
	private List<JIRAQueryFragment> queryFragment;
	private String name;

	JIRAManualFilter(final String name, List<JIRAQueryFragment> queryFragment) {
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
	public String toHTML(){
		String html = "<html><br>";
		for (JIRAQueryFragment fragment : queryFragment){
			html += fragment.getName() + "<br>";

			//html += "&nbsp;" + fragment.getMap().get("key") + "<br>";
		}

		html += "</html>";
		return html;
	}
}
