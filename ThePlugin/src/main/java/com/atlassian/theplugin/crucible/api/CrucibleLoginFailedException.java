package com.atlassian.theplugin.crucible.api;

/**
 * Thrown when the login operation fails in an orderly fashion - Bamboo returns a valid exception message.
 */
public class CrucibleLoginFailedException extends CrucibleLoginException {
	public CrucibleLoginFailedException(String message) {
		super(message);
	}
}