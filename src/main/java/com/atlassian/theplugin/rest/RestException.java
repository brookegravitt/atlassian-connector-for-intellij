package com.atlassian.theplugin.rest;

/**
 * Bamboo excepton related to session expired event process.
 */
public class RestException extends Exception {

	public RestException(String message) {
		super(message);
	}

	public RestException(Throwable throwable) {
		super(throwable);
	}

	public RestException(String message, Throwable throwable) {
		super(message, throwable);
	}
}