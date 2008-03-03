package com.atlassian.theplugin.bamboo;

public interface TestDetails {
	String getTestClassName();
	String getTestMethodName();
	double getTestDuration();
	TestResult getTestResult();
	String getErrors();
}
