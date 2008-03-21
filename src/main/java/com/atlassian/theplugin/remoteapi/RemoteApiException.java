package com.atlassian.theplugin.remoteapi;

/**
 * Bamboo excepton related to session expired event process.
 */
public class RemoteApiException extends Exception {

	public RemoteApiException(String message) {
		super(message);
	}

	public RemoteApiException(Throwable throwable) {
		super(throwable);
	}

	public RemoteApiException(String message, Throwable throwable) {
		super(message, throwable);
	}
}