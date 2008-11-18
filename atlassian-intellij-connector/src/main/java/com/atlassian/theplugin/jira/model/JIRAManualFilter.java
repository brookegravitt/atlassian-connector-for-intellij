package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.jira.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * User: pmaruszak
 */
public final class JIRAManualFilter {
	private enum QueryElement {
		PROJECT("Project"), ISSUE_TYPE("Issue Type"), FIX_FOR("Fix For"),
		COMPONENTS("Components"), AFFECTS_VERSIONS("Affects Versions"), REPORTER("Reporter"),
		ASSIGNEE("Assignee"), STATUS("Status"), RESOLUTIONS("Resolutions"),
		PRIORITIES("Priorities"), UNKNOWN("Unknown");
		private String name;

		QueryElement(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}
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
		String html = "<html><table>";
		TreeMap<QueryElement, ArrayList<String>> map = groupBy();

		for (QueryElement element : map.keySet()) {
			html += "<tr>&nbsp;<td align='right'>" + element.getName() + ":</td>&nbsp;<td align='left'>";
			for (String value : map.get(element)) {
				html += value + ", ";
			}
			html = html.substring(0, html.length() - 2);
			html += "</td></tr>";
		}

		html += "</table></html>";
		return html;
	}

	private TreeMap<QueryElement, ArrayList<String>> groupBy() {
		TreeMap<QueryElement, ArrayList<String>> map = new TreeMap<QueryElement, ArrayList<String>>();

		for (JIRAQueryFragment fragment : queryFragment) {
			if (fragment instanceof JIRAProjectBean) {
				addValueToMap(map, QueryElement.PROJECT, fragment.getName());
			} else if (fragment instanceof JIRAIssueTypeBean) {
				addValueToMap(map, QueryElement.ISSUE_TYPE, fragment.getName());
			} else if (fragment instanceof JIRAStatusBean) {
				addValueToMap(map, QueryElement.STATUS, fragment.getName());
			} else if (fragment instanceof JIRAPriorityBean) {
				addValueToMap(map, QueryElement.PRIORITIES, fragment.getName());
			} else if (fragment instanceof JIRAResolutionBean) {
				addValueToMap(map, QueryElement.RESOLUTIONS, fragment.getName());
			} else if (fragment instanceof JIRAFixForVersionBean) {
				addValueToMap(map, QueryElement.FIX_FOR, fragment.getName());
			} else if (fragment instanceof JIRAComponentBean) {
				addValueToMap(map, QueryElement.COMPONENTS, fragment.getName());
			} else if (fragment instanceof JIRAVersionBean) {
				addValueToMap(map, QueryElement.AFFECTS_VERSIONS, fragment.getName());
			} else if (fragment instanceof JIRAAssigneeBean) {
				addValueToMap(map, QueryElement.ASSIGNEE, fragment.getName());
			} else if (fragment instanceof JIRAReporterBean) {
				addValueToMap(map, QueryElement.REPORTER, fragment.getName());
			} else {
				addValueToMap(map, QueryElement.UNKNOWN, fragment.getName());
			}


		}
		return map;

	}

	private void addValueToMap(final TreeMap<QueryElement, ArrayList<String>> map, final QueryElement key, final String value) {
		if (!map.containsKey(key)) {
			map.put(key, new ArrayList<String>());
		}
		map.get(key).add(value);
	}


}
