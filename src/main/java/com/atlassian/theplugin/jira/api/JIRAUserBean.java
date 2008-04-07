package com.atlassian.theplugin.jira.api;

import java.util.Map;

public abstract class JIRAUserBean extends AbstractJIRAConstantBean {
	protected String value = "";

	public JIRAUserBean() {
		super();
	}

	public JIRAUserBean(long id, String name, String value) {
		super(id, name, null);
		this.value = value;
	}

	public JIRAUserBean(Map map) {
		super(map);
		value = (String) map.get("value");
    }

	public Map<String, String> getMap() {
		Map<String, String> map = super.getMap();
		map.put("value", getValue());
		return map;
	}

	public String getValue() {
		return value;
	}

	public abstract String getQueryStringFragment();
}