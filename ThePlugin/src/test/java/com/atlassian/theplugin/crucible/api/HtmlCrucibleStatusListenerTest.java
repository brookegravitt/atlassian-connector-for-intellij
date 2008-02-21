package com.atlassian.theplugin.crucible.api;

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
import java.util.*;

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
    private static final String DEFAULT_SERVER_URL = "http://test.atlassian.com/crucible/";
    private static final String DEFAULT_PROJECT_NAME = "CR";
	private static final String DEFAULT_PLAN_ID_2 = "CR-ID";
	private static final String DEFAULT_AUTHOR = "AUTHOR1";
	private static final String DEFAULT_CREATOR = "DEFAULT_CREATOR";
	private static final String DEFAULT_DESCRIPTION = "DEFAULT_DESCRIPTION";
	private static final String DEFAULT_MODERATOR = "DEFAULT_MODERATOR";
	private static final String DEFAULT_REVIEW_NAME = "DEFAULT_REVIEW_NAME";
	private static final String DEFAULT_REVIEWER = "DEFAULT_REVIEWER";

	private static final String DEFAULT_PERM_ID = "CR-";


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
		server.setUrlString(DEFAULT_SERVER_URL);
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
                "<html>" + HtmlCrucibleStatusListener.BODY_WITH_STYLE + "No reviews at this time.</body></html>",
                output.htmlPage);
	}

	public void testSingleSuccessResult() throws Exception {
		Collection<ReviewDataInfo> reviewInfo = new ArrayList<ReviewDataInfo>();

		reviewInfo.add(generateReviewDataInfo("1"));
		testedListener.updateReviews(reviewInfo);


		HtmlTable table = output.response.getTheTable();


		testReviewTable(table, 1);

	}

	/*@todo: change */
	@SuppressWarnings("unchecked")
	private static void testReviewTable(HtmlTable table, int numberOfReviews) throws Exception {
		StringBuilder sb = new StringBuilder();
		//first two are information about number of reviews + table header 
		for (int i = 2; i < table.getRowCount(); i++) {
			assertEquals(5, table.getRow(i).getCells().size());
		}

		assertEquals(table.getRowCount(), numberOfReviews + 2);


		if (table.getRowCount() > 0) {
			assertEquals("<tr><td colspan=\"5\">Currently<b>" + numberOfReviews + " open code reviews</b>for you.<br/>&#160;</td></tr>", trimWhitespace(table.getRow(0).asXml()));
			assertEquals("<tr><th>Key</th><th>Summary</th><th>Author</th><th>State</th><th>Reviewers</th></tr>", trimWhitespace(table.getRow(1).asXml()));
		}
		for (int i = 1; i <= numberOfReviews; i++) {
			/*sb.append("<tr><td valign=\"top\"><b><font color=\"blue\"><a href=\"");
			sb.append(DEFAULT_SERVER_URL);
			sb.append("\">");
			sb.append(DEFAULT_PERM_ID + i);
			sb.append("</a></font></b></td>");
			sb.append("<td valign=\"top\">" + DEFAULT_REVIEW_NAME + i + "</td>");
			sb.append("<td valign=\"top\">" + DEFAULT_AUTHOR + i + "</td>");
			sb.append("<td valign=\"top\">" + DEFAULT_STATE + "</td>");
			sb.append("<td valign=\"top\">");
			sb.append(DEFAULT_REVIEWER);
			sb.append("</td></tr>");

			assertEquals(sb.toString(), trimWhitespace(table.getRow(i + 2 - 1).asXml()));*/

		}


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
		rd.setCreator(DEFAULT_CREATOR);
		rd.setDescription(DEFAULT_DESCRIPTION);
		rd.setModerator(DEFAULT_MODERATOR);
		rd.setName(DEFAULT_PROJECT_NAME);

		PermId permId = new PermId();
		permId.setId(DEFAULT_PERM_ID + suffix);
		rd.setPermaId(permId);
		rd.setProjectKey(DEFAULT_PROJECT_KEY + suffix);
		rd.setState(DEFAULT_STATE);

		ArrayList reviewers = new ArrayList<String>();
		reviewers.add(DEFAULT_REVIEW_NAME + suffix);

		ReviewDataInfoImpl rdi = new ReviewDataInfoImpl(rd, reviewers, server);
		return rdi;
	}



	public static Test suite() {
		return new TestSuite(HtmlCrucibleStatusListenerTest.class);
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