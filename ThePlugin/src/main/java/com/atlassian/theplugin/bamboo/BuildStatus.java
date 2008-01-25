package com.atlassian.theplugin.bamboo;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:27:30 PM
 * To change this template use File | Settings | File Templates.
 */
public enum BuildStatus {
	SUCCESS,
	FAILED,  // build error
	ERROR   // ststus retrieval error
}
