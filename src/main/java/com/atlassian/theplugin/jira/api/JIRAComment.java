package com.atlassian.theplugin.jira.api;

import java.util.Calendar;

public interface JIRAComment {
	String getId();
	String getAuthor();
	String getBody();
	Calendar getCreationDate();
}
