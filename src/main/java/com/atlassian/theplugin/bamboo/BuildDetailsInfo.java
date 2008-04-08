/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
