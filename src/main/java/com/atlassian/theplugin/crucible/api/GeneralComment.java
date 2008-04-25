package com.atlassian.theplugin.crucible.api;

import java.util.Date;

public interface GeneralComment {
	String getMessage();

	boolean isDraft();

	boolean isDeleted();

	boolean isDefectRaised();

	boolean isDefectApproved();

	String getUser();

	Date getCreateDate();
}
