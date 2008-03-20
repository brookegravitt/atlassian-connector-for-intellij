package com.atlassian.theplugin.rest;

/**
 * Bamboo excepton related to session expired event process.
 */
public class RestSessionExpiredException extends RestException {

	public RestSessionExpiredException(String message) {
		super(message);
	}

	public RestSessionExpiredException(String message, Throwable throwable) {
		super(message, throwable);
	}
}