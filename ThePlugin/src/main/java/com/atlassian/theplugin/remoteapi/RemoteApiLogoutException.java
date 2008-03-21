package com.atlassian.theplugin.remoteapi;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-06
 * Time: 12:25:28
 * To change this template use File | Settings | File Templates.
 */
public class RemoteApiLogoutException extends RemoteApiException {
	public RemoteApiLogoutException(String message) {
		super(message);
	}

	public RemoteApiLogoutException(Throwable throwable) {
		super(throwable);
	}

	public RemoteApiLogoutException(String message, Throwable throwable) {
		super(message, throwable);
	}
}