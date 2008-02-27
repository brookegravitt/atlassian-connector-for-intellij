package com.atlassian.theplugin.exception;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Feb 19, 2008
 * Time: 11:48:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class VersionServiceException extends ThePluginException {
	public VersionServiceException(String s, Throwable e) {
		super(s, e);
	}

	public VersionServiceException(String s) {
		super(s);
	}
}
