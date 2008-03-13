package com.atlassian.theplugin.configuration;

/**
 * Exception thrown when the user has not provided any password for the server and this causes login error.
 */
public class ServerPasswordNotProvidedException extends Exception {
	public ServerPasswordNotProvidedException() {
		super();
	}
}
