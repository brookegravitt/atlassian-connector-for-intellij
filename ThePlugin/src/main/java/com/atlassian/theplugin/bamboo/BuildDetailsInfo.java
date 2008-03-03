package com.atlassian.theplugin.bamboo;

import java.util.ArrayList;
import java.util.List;

public class BuildDetailsInfo implements BuildDetails {
	private String vcsRevisionKey;
	private List<TestDetails> successfulTests;
	private List<TestDetails> failedTests;
	private List<Commit> commitInfo;

	public BuildDetailsInfo() {
		successfulTests = new ArrayList<TestDetails>();
		failedTests = new ArrayList<TestDetails>();
		commitInfo = new ArrayList<Commit>();
	}

	public String getVcsRevisionKey() {
		return vcsRevisionKey;
	}

	public void setVcsRevisionKey(String vcsRevisionKey) {
		this.vcsRevisionKey = vcsRevisionKey;
	}

	public List<TestDetails> getSuccessfulTestDetails() {
		return successfulTests;
	}

	public void setSuccessfulTests(List<TestDetails> successfulTests) {
		this.successfulTests = successfulTests;
	}

	public void addSuccessfulTest(TestDetails test) {
		successfulTests.add(test);
	}

	public List<TestDetails> getFailedTestDetails() {
		return failedTests;
	}

	public void setFailedTests(List<TestDetails> failedTests) {
		this.failedTests = failedTests;
	}

	public void addFailedTest(TestDetails test) {
		failedTests.add(test);
	}

	public List<Commit> getCommitInfo() {
		return commitInfo;
	}

	public void setCommitInfo(List<Commit> commitInfo) {
		this.commitInfo = commitInfo;
	}

	public void addCommitInfo(CommitInfo commit) {
		commitInfo.add(commit);
	}
}
