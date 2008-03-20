package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.rest.RestException;

/**
 * Generic Bamboo Excepion.
 */
public class BambooException extends RestException {

	public BambooException(String message) {
		super(message);
	}

	public BambooException(String message, Throwable throwable) {
		super(message, throwable);
	}
}