package com.atlassian.theplugin.remoteapi;

/**
 * Bamboo excepton related to session expired event process.
 */
public class RemoteApiSessionExpiredException extends RemoteApiException {

	public RemoteApiSessionExpiredException(String message) {
		super(message);
	}

	public RemoteApiSessionExpiredException(String message, Throwable throwable) {
		super(message, throwable);
	}
}