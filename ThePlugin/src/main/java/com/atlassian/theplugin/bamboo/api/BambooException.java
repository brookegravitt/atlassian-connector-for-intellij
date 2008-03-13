package com.atlassian.theplugin.bamboo.api;

/**
 * Generic Bamboo Excepion.
 */
public class BambooException extends Exception {

	public BambooException(String message) {
		super(message);
	}

	public BambooException(String message, Throwable throwable) {
		super(message, throwable);
	}
}