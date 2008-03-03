package com.atlassian.theplugin.bamboo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommitInfo implements Commit {
	private String author;
	private Date commitDate;
	private String comment;
	private List<CommitFile> files;

	public CommitInfo() {
		files = new ArrayList<CommitFile>();
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}

	public List<CommitFile> getFiles() {
		return files;
	}

	public void setFiles(List<CommitFile> files) {
		this.files = files;
	}

	public void addCommitFile(CommitFile file) {
		files.add(file);
	}
}
