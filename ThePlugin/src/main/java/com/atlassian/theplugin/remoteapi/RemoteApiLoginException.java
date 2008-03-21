package com.atlassian.theplugin.remoteapi;

/**
 * Bamboo excepton related to login process.
 */
public class RemoteApiLoginException extends RemoteApiException {

	public RemoteApiLoginException(String message) {
		super(message);
	}

	public RemoteApiLoginException(String message, Throwable throwable) {
		super(message, throwable);
	}
}