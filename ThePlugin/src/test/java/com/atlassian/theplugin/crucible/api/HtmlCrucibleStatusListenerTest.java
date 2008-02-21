package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.bamboo.HtmlBambooStatusListenerTest;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.crucible.CrucibleStatusDisplay;
import com.atlassian.theplugin.crucible.HtmlCrucibleStatusListener;
import com.atlassian.theplugin.crucible.ReviewDataInfo;
import com.atlassian.theplugin.crucible.ReviewDataInfoImpl;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId;
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
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Feb 20, 2008
 * Time: 4:27:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlCrucibleStatusListenerTest extends TestCase {

	private StatusListenerResultCatcher output;
	private HtmlCrucibleStatusListener testedListener;
	final static ServerBean server = new ServerBean();

	private static final String DEFAULT_ERROR_MESSAGE = "default error message";
    private static final String DEFAULT_SERVER_URL = "http://test.atlassian.com/crucible";
    private static final String DEFAULT_PROJECT_NAME = "CR";
	private static final String DEFAULT_PLAN_ID_2 = "CR-ID";
	private static final String DEFAULT_AUTHOR = "AUTHOR1";
	private static final String DEFAULT_CREATOR = "DEFAULT_CREATOR";
	private static final String DEFAULT_DESCRIPTION = "DEFAULT_DESCRIPTION";
	private static final String DEFAULT_MODERATOR = "DEFAULT_MODERATOR";
	private static PermId DEFAULT_PERM_ID;

	static {
		DEFAULT_PERM_ID = new PermId();
		DEFAULT_PERM_ID.setId("CR-0001");
	}



	private static final String DEFAULT_PROJECT_KEY = "DEFAULT_PROJECT_KEY";
	private static final State DEFAULT_STATE = State.REVIEW;


	protected void tearDown() throws Exception {
		output = null;
		testedListener = null;
		super.tearDown();
	}

	protected void setUp() throws Exception {
		super.setUp();

		output = new StatusListenerResultCatcher();
        server.setName("Test Server");
        testedListener = new HtmlCrucibleStatusListener(output) {
            protected Server getServerFromUrl(String serverUrl)   {
                return server;
            }
        };
	}

	public void testNullStatusCollection() throws Exception {
		testedListener.updateReviews(null);
		assertEquals(1, output.count);
		assertEquals(
                "<html>" + HtmlCrucibleStatusListener.BODY_WITH_STYLE + "No reviews at this time.</body></html>",
                output.htmlPage);

		int i=1;
	}

	public void testEmptyStatusCollection() throws Exception {
		testedListener.updateReviews(new ArrayList<ReviewDataInfo>());
		assertEquals(1, output.count);
        assertEquals(
                "<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + "\"No reviews at this time.</body></html>",
                output.htmlPage);
	}

	public void testSingleSuccessResult() throws Exception {
		Collection<ReviewDataInfo> reviewInfo = new ArrayList<ReviewDataInfo>();

		reviewInfo.add(generateReviewDataInfo(""));
		testedListener.updateReviews(reviewInfo);


		HtmlTable table = output.response.getTheTable();
		assertEquals(2, table.getRowCount());

		testReviewRow(table.getRow(1));

	}

	/*@todo: change */
	@SuppressWarnings("unchecked")
	private static void testReviewRow(HtmlTableRow tableRow) throws Exception {
		List<HtmlTableCell> cells = tableRow.getCells();
		assertEquals(3, cells.size());

         /*assertEquals("<tr><td valign=\"top\"><b><font color=blue><a href='\" + DEFAULT_SERVER_URL + "'>" + PERM_ID + "</a></font></b></td>" +
			"<td valign=\"top\">" + REVIEW_NAME + "</td>" +
			"<td valign=\"top\">" + REVIEW_AUTHOR + "</td>" +
			"<td valign=\"top\">" + REVIEW_STATE + "</td>",
					trimWhitespace(cells.get(0).asXml());

		assertEquals(DEFAULT_PROJECT_NAME + " " + DEFAULT_BUILD_NAME + " > PLAN-ID-777", cells.get(1).asText());

		String buildTime = cells.get(2).asText().trim();
		assertTrue(buildTime.length() > 1);
		assertFalse("---".equals(buildTime));*/

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

	public static ReviewDataInfo generateReviewDataInfo(String suffix) {
		ReviewDataBean rd = new ReviewDataBean();
		rd.setAuthor(DEFAULT_AUTHOR + suffix);
		rd.setCreator(DEFAULT_CREATOR + suffix);
		rd.setDescription(DEFAULT_DESCRIPTION + suffix);
		rd.setModerator(DEFAULT_MODERATOR + suffix);
		rd.setName(DEFAULT_PROJECT_NAME + suffix);
		rd.setPermaId(DEFAULT_PERM_ID);
		rd.setProjectKey(DEFAULT_PROJECT_KEY + suffix);
		rd.setState(DEFAULT_STATE);

		ArrayList reviewers = new ArrayList<String>();
		reviewers.add("ala1");
		reviewers.add("ala2");

		ReviewDataInfoImpl rdi = new ReviewDataInfoImpl(rd, reviewers, server);
		return rdi;
	}



	public static Test suite() {
		return new TestSuite(HtmlBambooStatusListenerTest.class);
	}


}

class StatusListenerResultCatcher implements CrucibleStatusDisplay {
	public String htmlPage;
	public ResponseWrapper response;

	public int count;

    public String getHtmlPage()
    {
        return htmlPage;
    }

	public void updateCrucibleStatus(String htmlPage) {

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