package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.commons.VersionedFileDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 11, 2008
 * Time: 3:05:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewedFileDescriptor extends VersionedFileDescriptor {

	private int numberOfComments = 0;

	/**
	 * How many people think the file contains defects
	 */
	private int numberOfDefects = 0;

	public int getNumberOfComments() {
		return numberOfComments;
	}

	public void setNumberOfComments(int numberOfComments) {
		this.numberOfComments = numberOfComments;
	}

	public int getNumberOfDefects() {
		return numberOfDefects;
	}

	public void setNumberOfDefects(int numberOfDefects) {
		this.numberOfDefects = numberOfDefects;
	}

}
