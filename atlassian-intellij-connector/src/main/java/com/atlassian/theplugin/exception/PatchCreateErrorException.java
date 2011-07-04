/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.theplugin.exception;

import com.atlassian.theplugin.commons.exception.ThePluginException;

public class PatchCreateErrorException extends ThePluginException {
	public PatchCreateErrorException(String message) {
		super(message);
	}

	public PatchCreateErrorException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
