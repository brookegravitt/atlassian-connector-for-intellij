package com.atlassian.theplugin.jira.api;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.Map;
import java.util.HashMap;

public class JIRAProjectBean implements JIRAProject {
	private String name;
	private String key;
	private String url;
	private long id;
	private String description;
	private String lead;

	public JIRAProjectBean() {
	}

	public JIRAProjectBean(Map projMap) {
		name = (String) projMap.get("name");
		key = (String) projMap.get("key");
		description = (String) projMap.get("description");
		url = (String) projMap.get("url");
		lead = (String) projMap.get("lead");
		id = Long.valueOf((String) projMap.get("id"));
	}

	public JIRAProjectBean(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", getName());
		map.put("id", Long.toString(id));
		map.put("key", getKey());
		map.put("description", getDescription());
		map.put("url", getUrl());
		map.put("lead", getLead());
		map.put("filterTypeClass", this.getClass().getName());
		return map;
	}

	public String getKey() {
		return key;
	}

	public String getUrl() {
		return url;
	}

	public long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getLead() {
		return lead;
	}

	@Transient
	public String getQueryStringFragment() {
		return "pid=" + id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLead(String lead) {
		this.lead = lead;
	}
}
