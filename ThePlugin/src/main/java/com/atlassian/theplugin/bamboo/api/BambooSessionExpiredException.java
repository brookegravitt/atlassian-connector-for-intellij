package com.atlassian.theplugin.bamboo.api;

/**
 * Bamboo excepton related to session expired event process.
 */
public class BambooSessionExpiredException extends BambooException {

	public BambooSessionExpiredException(String message) {
		super(message);
	}

	public BambooSessionExpiredException(String message, Throwable throwable) {
		super(message, throwable);
	}
}