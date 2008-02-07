package com.atlassian.theplugin.crucible.api;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-07
 * Time: 10:52:25
 * To change this template use File | Settings | File Templates.
 */
public interface CrucibleSession {
	void login(String userName, String password) throws CrucibleLoginException;

	void logout() throws CrucibleLogoutException;
}
