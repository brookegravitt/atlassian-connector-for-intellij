package com.atlassian.theplugin.jira.api;

public class JIRACommentBean implements JIRAComment {
	private String id;
	private String author;
	private String body;

	public JIRACommentBean(String id, String author, String body) {
		this.id = id;
		this.author = author;
		this.body = body;
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
}
