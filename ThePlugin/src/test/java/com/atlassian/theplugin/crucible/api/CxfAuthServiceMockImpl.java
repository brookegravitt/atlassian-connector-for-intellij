package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-08
 * Time: 14:55:03
 * To change this template use File | Settings | File Templates.
 */
public class CxfAuthServiceMockImpl implements RpcAuthServiceName {

	private String token;
	public static final String INVALID_LOGIN = "invalidLogin";
	public static final String VALID_LOGIN = "validLogin";
	public static final String INVALID_PASSWORD = "invalidPassword";
	public static final String VALID_PASSWORD = "validPassword";
	public static final String VALID_URL = "http://localhost:9000";

	public String login(String userName, String password) {

		if (userName.equals(INVALID_LOGIN) || password.equals(INVALID_PASSWORD)) {
			return null;
		} else if (userName.equals(VALID_LOGIN) && password.equals(VALID_PASSWORD)) {
			return "test token";
		} else {
			return null;
		}
	}

	public void logout(String arg0) {	
	}
}
