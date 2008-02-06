package com.atlassian.theplugin.crucible.api;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 15:10:39
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleSession {
	private String crucibleUrl;

	public CrucibleSession(String baseUrl) {
		this.crucibleUrl = baseUrl;
	}

	public void login(String userName, String password) throws CrucibleLoginException {

	}

	public void logout() {
		
	}
}
