package com.atlassian.theplugin.bamboo.api;

/**
 * Thrown when the login operation fails in an orderly fashion - Bamboo returns a valid exception message.
 */
public class BambooLoginFailedException extends BambooLoginException {
	public BambooLoginFailedException(String message) {
		super(message);
	}
}
