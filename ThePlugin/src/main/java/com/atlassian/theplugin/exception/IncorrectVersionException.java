package com.atlassian.theplugin.exception;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 3, 2008
 * Time: 4:50:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class IncorrectVersionException extends ThePluginException {

	public IncorrectVersionException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public IncorrectVersionException(String message) {
		super(message);
	}
}
