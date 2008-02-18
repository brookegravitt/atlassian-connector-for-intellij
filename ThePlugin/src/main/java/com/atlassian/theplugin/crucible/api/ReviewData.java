package com.atlassian.theplugin.crucible.api;

public interface ReviewData {
	String getAuthor();

	String getCreator();

	String getDescription();

	String getModerator();

	String getName();

	PermId getParentReview();

	PermId getPermaId();

	String getProjectKey();

	String getRepoName();

	State getState();
}