package com.atlassian.theplugin.bamboo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Collection;

/**
 * HtmlBambooStatusListener Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/30/2008</pre>
 */
public class HtmlBambooStatusListenerTest extends TestCase {

	private StatusListenerResultCatcher output;
	private HtmlBambooStatusListener testedListener;

	public static final String DEFAULT_PLAN_ID = "PLAN-ID";
	public static final int DEFAULT_BUILD_NO = 777;
	public static final String DEFAULT_BUILD_NAME = "Plan name";


	protected void tearDown() throws Exception {
		output = null;
		testedListener = null;
		super.tearDown();
	}

	protected void setUp() throws Exception {
		super.setUp();

		output = new StatusListenerResultCatcher();
		testedListener = new HtmlBambooStatusListener(output);
	}

	public void testNullStatusCollection() throws Exception {
		testedListener.updateBuildStatuses(null);
		assertEquals(1, output.count);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);
		assertEquals("<html><body>No plans defined.</body></html>", output.htmlPage);
	}

	public void testEmptyStatusCollection() throws Exception {
		testedListener.updateBuildStatuses(new ArrayList<BambooBuild>());
		assertEquals(1, output.count);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);
		assertEquals("<html><body>No plans defined.</body></html>", output.htmlPage);
	}

	public void testSingleStatusResult() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();

		buildInfo.add(generateBuildInfo(BuildStatus.BUILD_SUCCEED));
		testedListener.updateBuildStatuses(buildInfo);
		assertSame(BuildStatus.BUILD_SUCCEED, output.buildStatus);

		buildInfo.clear();
		buildInfo.add(generateBuildInfo(BuildStatus.BUILD_FAILED));
		testedListener.updateBuildStatuses(buildInfo);
		assertSame(BuildStatus.BUILD_FAILED, output.buildStatus);

		buildInfo.clear();
		buildInfo.add(generateBuildInfo(BuildStatus.UNKNOWN));
		testedListener.updateBuildStatuses(buildInfo);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);


	}


	private static BambooBuild generateBuildInfo(BuildStatus status) {
		BambooBuildInfo buildInfo = new BambooBuildInfo();

		buildInfo.setBuildKey(DEFAULT_PLAN_ID);
		buildInfo.setBuildName(DEFAULT_BUILD_NAME);
		buildInfo.setBuildNumber(String.valueOf(DEFAULT_BUILD_NO));

		switch (status) {
			case UNKNOWN:
				buildInfo.setBuildState("Unknown");
				break;
			case BUILD_SUCCEED:
				buildInfo.setBuildState("Successful");
				break;
			case BUILD_FAILED:
				buildInfo.setBuildState("Failed");
				break;
		}


		return buildInfo;
	}

	public static Test suite() {
		return new TestSuite(HtmlBambooStatusListenerTest.class);
	}


}

class StatusListenerResultCatcher implements BambooStatusDisplay {
	public BuildStatus buildStatus;
	public String htmlPage;

	public int count;

	public void updateBambooStatus(BuildStatus generalBuildStatus, String htmlPage) {
		buildStatus = generalBuildStatus;
		this.htmlPage = htmlPage;

		++count;
	}
}