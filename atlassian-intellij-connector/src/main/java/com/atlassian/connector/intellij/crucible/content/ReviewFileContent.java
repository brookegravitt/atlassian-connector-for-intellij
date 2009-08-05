package com.atlassian.connector.intellij.crucible.content;

/**
 * User: mwent
 * Date: Mar 13, 2009
 */
public class ReviewFileContent {
	private final byte[] content;

    public ReviewFileContent(final byte[] content) {
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}


}