package com.atlassian.theplugin.crucible.api;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-06
 * Time: 12:25:28
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleLogoutException extends CrucibleException {
	public CrucibleLogoutException(String message) {
		super(message);
	}

	public CrucibleLogoutException(Throwable throwable) {
		super(throwable);
	}
	
	public CrucibleLogoutException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
