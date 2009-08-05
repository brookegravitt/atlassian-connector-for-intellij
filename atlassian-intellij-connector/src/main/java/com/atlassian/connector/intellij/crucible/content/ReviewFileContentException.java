package com.atlassian.connector.intellij.crucible.content;

/**
 * User: mwent
 * Date: Mar 13, 2009
 * Time: 12:57:37 PM
 */
public class ReviewFileContentException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6060229636851982549L;

	public ReviewFileContentException() {
	}

	public ReviewFileContentException(final String message) {
		super(message);
	}

	public ReviewFileContentException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ReviewFileContentException(final Throwable cause) {
		super(cause);
	}
}
