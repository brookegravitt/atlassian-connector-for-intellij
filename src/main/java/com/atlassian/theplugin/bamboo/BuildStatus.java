package com.atlassian.theplugin.bamboo;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:27:30 PM
 * To change this template use File | Settings | File Templates.
 */
public enum BuildStatus {
    BUILD_SUCCEED,
    BUILD_FAILED,  // build error
    UNKNOWN   // ststus retrieval error
} 
