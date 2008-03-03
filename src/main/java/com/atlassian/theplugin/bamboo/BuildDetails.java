package com.atlassian.theplugin.bamboo;

import java.util.List;

public interface BuildDetails {
	String getVcsRevisionKey();
	List<TestDetails> getSuccessfulTestDetails();
	List<TestDetails> getFailedTestDetails();
	List<Commit> getCommitInfo();
}
