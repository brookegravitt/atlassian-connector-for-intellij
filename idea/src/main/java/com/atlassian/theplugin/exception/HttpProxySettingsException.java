package com.atlassian.theplugin.exception;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Apr 30, 2008
 * Time: 3:20:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class HttpProxySettingsException extends Exception {
		public HttpProxySettingsException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public HttpProxySettingsException(String message) {
		super(message);
	}
}
