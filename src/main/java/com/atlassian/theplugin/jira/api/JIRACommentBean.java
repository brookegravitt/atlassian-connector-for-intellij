package com.atlassian.theplugin.jira.api;

import java.util.Calendar;

public class JIRACommentBean implements JIRAComment {
	private String id;
	private String author;
	private String body;
	private Calendar created;

	public JIRACommentBean(String id, String author, String body, Calendar created) {
		this.id = id;
		this.author = author;
		this.body = body;
		this.created = created;
	}
	public String getId() {
		return id;
	}

	public String getAuthor() {
		return author;
	}

	public String getBody() {
		return body;  
	}

	public Calendar getCreationDate() {
		return created;
	}
}
