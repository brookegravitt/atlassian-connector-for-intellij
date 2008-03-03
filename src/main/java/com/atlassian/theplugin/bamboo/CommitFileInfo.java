package com.atlassian.theplugin.bamboo;


public class CommitFileInfo implements CommitFile {
	private String fileName;
	private String revision;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
}
