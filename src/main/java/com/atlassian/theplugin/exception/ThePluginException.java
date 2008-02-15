package com.atlassian.theplugin.exception;

public class ThePluginException extends Exception {

	public ThePluginException(String message) {
		super(message);
	}

	ThePluginException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
