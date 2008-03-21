package com.atlassian.theplugin.api;

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