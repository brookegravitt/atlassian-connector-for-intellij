package com.atlassian.theplugin.bamboo.api;

/**
 * Bamboo excepton related to login process.
 */
public class BambooLoginException extends BambooException {

	public BambooLoginException(String message) {
		super(message);
	}

	public BambooLoginException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
