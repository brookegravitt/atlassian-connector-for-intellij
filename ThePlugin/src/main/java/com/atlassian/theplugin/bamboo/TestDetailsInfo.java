package com.atlassian.theplugin.bamboo;

public class TestDetailsInfo implements TestDetails {
	private String testClassName;
	private String testMethodName;
	private double testDuration;
	private TestResult testResult;
	private String testErrors;


	public String getTestClassName() {
		return testClassName;
	}

	public String getTestMethodName() {
		return testMethodName;
	}

	public double getTestDuration() {
		return testDuration;
	}

	public TestResult getTestResult() {
		return testResult;
	}

	public String getErrors() {
		return testErrors;
	}

	public void setTestClassName(String testClassName) {
		this.testClassName = testClassName;
	}

	public void setTestDuration(double testDuration) {
		this.testDuration = testDuration;
	}

	public void setTestErrors(String testErrors) {
		this.testErrors = testErrors;
	}

	public void setTestMethodName(String testMethodName) {
		this.testMethodName = testMethodName;
	}

	public void setTestResult(TestResult testResult) {
		this.testResult = testResult;
	}


}
