package com.atlassian.theplugin.rest;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-06
 * Time: 12:25:28
 * To change this template use File | Settings | File Templates.
 */
public class RestLogoutException extends RestException {
	public RestLogoutException(String message) {
		super(message);
	}

	public RestLogoutException(Throwable throwable) {
		super(throwable);
	}

	public RestLogoutException(String message, Throwable throwable) {
		super(message, throwable);
	}
}