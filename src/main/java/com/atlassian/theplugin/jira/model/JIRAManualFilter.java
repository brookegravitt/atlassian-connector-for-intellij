package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.*;

import java.util.ArrayList;
import java.util.HashMap;
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

	public String toHTML() {
		String html = "<html><br>";
		HashMap<String, ArrayList<String>> map = groupBy(queryFragment);


		for (String groupName : map.keySet()) {
			html += groupName;
			for (String value : map.get(groupName)) {
				html += "&nbsp;<i>" + value + "</i>,";
			}
			
			html = html.substring(0, html.length() - 1);
			html += "<br>";

		}

		html += "</html>";
		return html;
	}

	private HashMap<String, ArrayList<String>> groupBy(final List<JIRAQueryFragment> queryFragment) {
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

		for (JIRAQueryFragment fragment : queryFragment) {
			if (fragment instanceof JIRAProjectBean) {

				addValueToMap(map, "Project:", fragment.getName());

			} else if (fragment instanceof JIRAIssueTypeBean) {
				addValueToMap(map, "Issue Type:", fragment.getName());

			} else if (fragment instanceof JIRAStatusBean) {
				addValueToMap(map, "Status:", fragment.getName());
			} else if (fragment instanceof JIRAPriorityBean) {
				addValueToMap(map, "Priority:", fragment.getName());
			} else if (fragment instanceof JIRAResolutionBean) {
				addValueToMap(map, "Resolution:", fragment.getName());
			} else if (fragment instanceof JIRAFixForVersionBean) {
				addValueToMap(map, "Fix For:", fragment.getName());
			} else if (fragment instanceof JIRAComponentBean) {
				addValueToMap(map, "Components:", fragment.getName());
			} else if (fragment instanceof JIRAVersionBean) {
				addValueToMap(map, "Versions:", fragment.getName());
			} else if (fragment instanceof JIRAAssigneeBean) {
				addValueToMap(map, "Assignee:", fragment.getName());
			} else if (fragment instanceof JIRAReporterBean) {
				addValueToMap(map, "Reporter:", fragment.getName());
			} else {
				addValueToMap(map, "unknown", fragment.getName());
			}


		}
		return map;

	}

	private void addValueToMap(final HashMap<String, ArrayList<String>> map, final String key, final String value){
		if (!map.containsKey(key)){
			map.put(key, new ArrayList<String>());
		}
		map.get(key).add(value);
	}


}
