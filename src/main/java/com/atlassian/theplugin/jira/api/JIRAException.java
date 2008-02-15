package com.atlassian.theplugin.jira.api;

/**
 * Generic Bamboo Excepion.
 */
public class JIRAException extends Exception {

	public JIRAException(String message) {
		super(message);
	}

	public JIRAException(String message, Throwable throwable) {
		super(message, throwable);
	}
}