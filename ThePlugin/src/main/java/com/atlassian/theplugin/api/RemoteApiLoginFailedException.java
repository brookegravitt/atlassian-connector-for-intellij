package com.atlassian.theplugin.api;

/**
 * Thrown when the login operation fails in an orderly fashion - Bamboo returns a valid exception message.
 */
public class RemoteApiLoginFailedException extends RemoteApiLoginException {
	public RemoteApiLoginFailedException(String message) {
		super(message);
	}
}