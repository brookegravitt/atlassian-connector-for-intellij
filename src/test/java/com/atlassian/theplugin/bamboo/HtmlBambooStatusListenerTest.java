package com.atlassian.theplugin.bamboo;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

	private static final String DEFAULT_PLAN_ID = "PLAN-ID";
	private static final int DEFAULT_BUILD_NO = 777;
	private static final String DEFAULT_BUILD_NAME = "Plan name";
	private static final String DEFAULT_ERROR_MESSAGE = "default error message";


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
		assertEquals("<html><body>No plans defined. <a href=\"http://theplugin-config\">Configure</a>.</body></html>", output.htmlPage);
	}

	public void testEmptyStatusCollection() throws Exception {
		testedListener.updateBuildStatuses(new ArrayList<BambooBuild>());
		assertEquals(1, output.count);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);
		assertEquals("<html><body>No plans defined. <a href=\"http://theplugin-config\">Configure</a>.</body></html>", output.htmlPage);
	}

	public void testSingleSuccessResult() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();

		buildInfo.add(generateBuildInfo(BuildStatus.BUILD_SUCCEED));
		testedListener.updateBuildStatuses(buildInfo);
		assertSame(BuildStatus.BUILD_SUCCEED, output.buildStatus);

		HtmlTable table = output.response.getTheTable();
		assertEquals(2, table.getRowCount());

		testSuccessRow(table.getRow(1));

	}

	public void testSingleFailedResult() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();
		buildInfo.add(generateBuildInfo(BuildStatus.BUILD_FAILED));
		testedListener.updateBuildStatuses(buildInfo);
		assertSame(BuildStatus.BUILD_FAILED, output.buildStatus);

		HtmlTable table = output.response.getTheTable();
		assertEquals(2, table.getRowCount());

		testFailedRow(table.getRow(1));

	}

	public void testSingleErrorResult() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();
		buildInfo.add(generateBuildInfo(BuildStatus.UNKNOWN));
		testedListener.updateBuildStatuses(buildInfo);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);

		HtmlTable table = output.response.getTheTable();
		assertEquals(2, table.getRowCount());

		testErrorRow(table.getRow(1));

	}

	@SuppressWarnings("unchecked")
	private static void testSuccessRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(5, cells.size());

		assertEquals(DEFAULT_PLAN_ID, cells.get(0).asText());
		assertEquals("build " + DEFAULT_BUILD_NO, cells.get(1).asText());
		assertEquals("success", cells.get(2).asText());

		String pollTime = cells.get(3).asText().trim();
		assertTrue(pollTime.length() > 1);
		assertFalse("---".equals(pollTime));

		String buildTime = cells.get(4).asText().trim();
		assertTrue(buildTime.length() > 1);
		assertFalse("---".equals(buildTime));
	}

	@SuppressWarnings("unchecked")
	private static void testFailedRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(5, cells.size());

		assertEquals(DEFAULT_PLAN_ID, cells.get(0).asText());
		assertEquals("build " + DEFAULT_BUILD_NO, cells.get(1).asText());
		assertEquals("failed", cells.get(2).asText());

		String pollTime = cells.get(3).asText().trim();
		assertTrue(pollTime.length() > 1);
		assertFalse("---".equals(pollTime));

		String buildTime = cells.get(4).asText().trim();
		assertTrue(buildTime.length() > 1);
		assertFalse("---".equals(buildTime));
	}

	@SuppressWarnings("unchecked")
	private static void testErrorRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(5, cells.size());

		assertEquals(DEFAULT_PLAN_ID, cells.get(0).asText());
		assertEquals("", cells.get(1).asText());
		assertEquals(DEFAULT_ERROR_MESSAGE, cells.get(2).asText());

		String pollTime = cells.get(3).asText().trim();
		assertTrue(pollTime.length() > 1);

		assertEquals("---", cells.get(4).asText().trim());
	}


	private static BambooBuild generateBuildInfo(BuildStatus status) {
		BambooBuildInfo buildInfo = new BambooBuildInfo();

		buildInfo.setBuildKey(DEFAULT_PLAN_ID);
		buildInfo.setBuildName(DEFAULT_BUILD_NAME);
		buildInfo.setBuildNumber(String.valueOf(DEFAULT_BUILD_NO));

		switch (status) {
			case UNKNOWN:
				buildInfo.setBuildState("Unknown");
				buildInfo.setMessage(DEFAULT_ERROR_MESSAGE);
				break;
			case BUILD_SUCCEED:
				buildInfo.setBuildState("Successful");
				buildInfo.setBuildTime(new Date());
				break;
			case BUILD_FAILED:
				buildInfo.setBuildState("Failed");
				buildInfo.setBuildTime(new Date());
				break;
		}
		buildInfo.setPollingTime(new Date());

		return buildInfo;
	}

	public static Test suite() {
		return new TestSuite(HtmlBambooStatusListenerTest.class);
	}


}

class StatusListenerResultCatcher implements BambooStatusDisplay {
	public BuildStatus buildStatus;
	public String htmlPage;
	public ResponseWrapper response;

	public int count;

	public void updateBambooStatus(BuildStatus generalBuildStatus, String htmlPage) {
		buildStatus = generalBuildStatus;
		this.htmlPage = htmlPage;

		++count;

		try {
			response = new ResponseWrapper(htmlPage);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


}

class ResponseWrapper {
	private HtmlPage thePage;
	private HtmlTable theTable;

	ResponseWrapper(String htmlPage) throws IOException {
		StringWebResponse swr = new StringWebResponse(htmlPage);
		WebClient wc = new WebClient();
		thePage = HTMLParser.parse(swr, new TopLevelWindow("", wc));
	}

	public HtmlPage getPage() {
		return thePage;
	}


	public HtmlTable getTheTable() throws Exception {
		if (theTable == null) {
			List tables = thePage.getByXPath("html/body/table");
			Assert.assertEquals(1, tables.size());
			theTable = (HtmlTable) tables.get(0);
		}
		return theTable;
	}

}