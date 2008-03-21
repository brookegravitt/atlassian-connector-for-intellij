package com.atlassian.theplugin.remoteapi;

/**
 * Bamboo excepton related to session expired event process.
 */
public class RemoteApiMalformedUrlException extends RemoteApiException {

	public RemoteApiMalformedUrlException(String message) {
		super(message);
	}

	public RemoteApiMalformedUrlException(Throwable throwable) {
		super(throwable);
	}

	public RemoteApiMalformedUrlException(String message, Throwable throwable) {
		super(message, throwable);
	}
}