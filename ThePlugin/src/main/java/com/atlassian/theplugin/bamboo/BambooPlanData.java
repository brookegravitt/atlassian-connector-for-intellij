package com.atlassian.theplugin.bamboo;

public class BambooPlanData implements BambooPlan {
	private String name;
	private String key;
	private boolean favourite;

	public BambooPlanData(String name, String key) {
		this.name = name;
		this.key = key;
	}

	public String getPlanName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlanKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isFavourite() {
		return favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}
}
