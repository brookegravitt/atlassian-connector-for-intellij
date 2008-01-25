package com.atlassian.theplugin.bamboo.api;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 14:31:38
 * To change this template use File | Settings | File Templates.
 */
public class BambooLoginException extends BambooException {

	public BambooLoginException(Throwable throwable) {
		super(throwable);
	}

	public BambooLoginException(String message) {
		super(message);
	}

	public BambooLoginException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
