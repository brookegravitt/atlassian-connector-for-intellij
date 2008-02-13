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
import java.util.*;

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
	private static final String DEFAULT_BUILD_NAME = "Default Plan";
	private static final String DEFAULT_ERROR_MESSAGE = "default error message";
    private static final String DEFAULT_SERVER_URL = "http://test.atlassian.com/bamboo";
    private static final String DEFAULT_PROJECT_NAME = "ThePlugin";


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
		assertEquals(
                "<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + "No plans defined.</body></html>",
                output.htmlPage);
	}

	public void testEmptyStatusCollection() throws Exception {
		testedListener.updateBuildStatuses(new ArrayList<BambooBuild>());
		assertEquals(1, output.count);
		assertSame(BuildStatus.UNKNOWN, output.buildStatus);
        assertEquals(
                "<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + "No plans defined.</body></html>",
                output.htmlPage);
	}

	public void testSingleSuccessResult() throws Exception {
		Collection<BambooBuild> buildInfo = new ArrayList<BambooBuild>();

		buildInfo.add(generateBuildInfo(BuildStatus.BUILD_SUCCEED));
		testedListener.updateBuildStatuses(buildInfo);

        System.out.println("output.getHtmlPage() = " + output.getHtmlPage());
        
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
		assertEquals(4, cells.size());

        assertEquals("<td><a href=\"" + DEFAULT_SERVER_URL + "/browse/PLAN-ID\"><img src=\"/icons/icn_plan_passed.gif\" height=\"16\" width=\"16\" border=\"0\" align=\"absmiddle\"/></a></td>", trimWhitespace(cells.get(0).asXml()));
		assertEquals(DEFAULT_PROJECT_NAME + " " + DEFAULT_BUILD_NAME + " > PLAN-ID-777", cells.get(1).asText());

		String pollTime = cells.get(2).asText().trim();
		assertTrue(pollTime.length() > 1);
		assertFalse("---".equals(pollTime));

		String buildTime = cells.get(3).asText().trim();
		assertTrue(buildTime.length() > 1);
		assertFalse("---".equals(buildTime));
	}

    private static String trimWhitespace(String s)
    {
        StringBuffer result = new StringBuffer("");
        for (StringTokenizer stringTokenizer = new StringTokenizer(s, "\n"); stringTokenizer.hasMoreTokens();)
        {
            result.append(stringTokenizer.nextToken().trim());
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
	private static void testFailedRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(4, cells.size());

        assertEquals("<td><a href=\"" + DEFAULT_SERVER_URL + "/browse/PLAN-ID\"><img src=\"/icons/icn_plan_failed.gif\" height=\"16\" width=\"16\" border=\"0\" align=\"absmiddle\"/></a></td>", trimWhitespace(cells.get(0).asXml()));
		assertEquals(DEFAULT_PROJECT_NAME + " " + DEFAULT_BUILD_NAME + " > PLAN-ID-777", cells.get(1).asText());

		String pollTime = cells.get(2).asText().trim();
		assertFalse("&nbsp;".equals(pollTime));

		String buildTime = cells.get(3).asText().trim();
		assertFalse("&nbsp;".equals(buildTime));
	}

	@SuppressWarnings("unchecked")
	private static void testErrorRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(4, cells.size());

        assertEquals("<td><a href=\"" + DEFAULT_SERVER_URL + "/browse/PLAN-ID\"><img src=\"/icons/icn_plan_disabled.gif\" height=\"16\" width=\"16\" border=\"0\" align=\"absmiddle\"/></a></td>", trimWhitespace(cells.get(0).asXml()));
		assertEquals(DEFAULT_ERROR_MESSAGE, cells.get(1).asText());
        assertEquals("<td><font color=\"ltgrey\">" + DEFAULT_ERROR_MESSAGE + "</font></td>", trimWhitespace(cells.get(1).asXml()));

		String pollTime = cells.get(2).asText().trim();
		assertTrue(pollTime.length() > 1);

		assertEquals("", cells.get(3).asText().trim());
	}


	private static BambooBuild generateBuildInfo(BuildStatus status) {
		BambooBuildInfo buildInfo = new BambooBuildInfo();

		buildInfo.setBuildKey(DEFAULT_PLAN_ID);
		buildInfo.setBuildName(DEFAULT_BUILD_NAME);
		buildInfo.setBuildNumber(String.valueOf(DEFAULT_BUILD_NO));
        buildInfo.setProjectName(DEFAULT_PROJECT_NAME);
        buildInfo.setServerUrl(DEFAULT_SERVER_URL);

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

    public String getHtmlPage()
    {
        return htmlPage;
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