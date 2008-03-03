package com.atlassian.theplugin.bamboo;

import java.util.Date;
import java.util.List;

public interface Commit {
	String getAuthor();

	String getComment();

	Date getCommitDate();

	List<CommitFile> getFiles();
}
