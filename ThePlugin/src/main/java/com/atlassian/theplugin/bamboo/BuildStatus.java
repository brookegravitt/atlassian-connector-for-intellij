package com.atlassian.theplugin.bamboo;

public enum BuildStatus {
    BUILD_SUCCEED,
    BUILD_FAILED,  // build error
    UNKNOWN,   // ststus retrieval error
	BUILD_DISABLED
} 
