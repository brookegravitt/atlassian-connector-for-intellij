package com.atlassian.theplugin.rest;

/**
 * Bamboo excepton related to login process.
 */
public class RestLoginException extends RestException {

	public RestLoginException(String message) {
		super(message);
	}

	public RestLoginException(String message, Throwable throwable) {
		super(message, throwable);
	}
}