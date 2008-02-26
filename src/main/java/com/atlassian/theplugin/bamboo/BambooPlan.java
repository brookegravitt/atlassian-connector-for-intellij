package com.atlassian.theplugin.bamboo;

public interface BambooPlan {
	String getPlanName();

	String getPlanKey();

	boolean isFavourite();

	boolean isEnabled();
}
