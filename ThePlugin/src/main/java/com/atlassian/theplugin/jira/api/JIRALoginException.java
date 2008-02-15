package com.atlassian.theplugin.jira.api;

/**
 * Generic Bamboo Excepion.
 */
public class JIRALoginException extends JIRAException {

	public JIRALoginException(String message) {
		super(message);
	}

	public JIRALoginException(String message, Throwable throwable) {
		super(message, throwable);
	}
}