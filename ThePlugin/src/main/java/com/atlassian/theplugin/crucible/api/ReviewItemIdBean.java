package com.atlassian.theplugin.crucible.api;

public class ReviewItemIdBean implements ReviewItemId{
	private String id;

	public ReviewItemIdBean() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
