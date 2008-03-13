package com.atlassian.theplugin.crucible.api;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-21
 * Time: 10:42:50
 * To change this template use File | Settings | File Templates.
 */
public class PermIdBean implements PermId {
	private String id;

	public PermIdBean() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
