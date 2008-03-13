package com.atlassian.theplugin.crucible.api;

public class RepositoryDataBean implements RepositoryData {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
