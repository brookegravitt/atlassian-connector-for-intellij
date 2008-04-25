package com.atlassian.theplugin.crucible.api;

public interface VersionedComment extends GeneralComment {
	PermId getPermId();

	ReviewItemId getReviewItemId();

	boolean isToLineInfo();

	int getToStartLine();

	int getToEndLine();

	boolean isFromLineInfo();	

	int getFromStartLine();

	int getFromEndLine();	
}