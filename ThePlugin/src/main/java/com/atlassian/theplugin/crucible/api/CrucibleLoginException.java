package com.atlassian.theplugin.crucible.api;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 15:29:46
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleLoginException extends CrucibleException {

	CrucibleLoginException(String message) {
		super(message);
	}

	CrucibleLoginException(Throwable throwable) {
		super(throwable);
	}

	CrucibleLoginException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
