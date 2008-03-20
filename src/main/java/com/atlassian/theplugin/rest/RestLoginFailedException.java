package com.atlassian.theplugin.rest;

/**
 * Thrown when the login operation fails in an orderly fashion - Bamboo returns a valid exception message.
 */
public class RestLoginFailedException extends RestLoginException {
	public RestLoginFailedException(String message) {
		super(message);
	}
}