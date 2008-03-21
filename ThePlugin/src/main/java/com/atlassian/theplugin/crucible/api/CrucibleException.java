package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.remoteapi.RemoteApiException;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:14:11
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleException extends RemoteApiException {

	public CrucibleException(String message) {
		super(message);
	}

	public CrucibleException(Throwable throwable) {
		super(throwable);
	}

	public CrucibleException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
