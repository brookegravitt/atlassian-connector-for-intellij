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

package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.crucible.CrucibleStatusDisplay;
import com.atlassian.theplugin.commons.crucible.HtmlCrucibleStatusListener;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class HtmlCrucibleStatusListenerTest extends TestCase {

	private StatusListenerResultCatcher output;
	private HtmlCrucibleStatusListener testedListener;
	private static final ServerBean server = new ServerBean();

    private static final String DEFAULT_SERVER_URL = "http://test.atlassian.com/crucible/";
    private static final String DEFAULT_PROJECT_NAME = "cru";
	private static final String DEFAULT_AUTHOR = "AUTHOR1";
	private static final String DEFAULT_CREATOR = "DEFAULT_CREATOR";
	private static final String DEFAULT_DESCRIPTION = "DEFAULT_DESCRIPTION";
	private static final String DEFAULT_MODERATOR = "DEFAULT_MODERATOR";
	private static final String DEFAULT_REVIEW_NAME = "cru";
	private static final String DEFAULT_REVIEWER = "cru";

	private static final String DEFAULT_PERM_ID = "CR-";


	private static final String DEFAULT_PROJECT_KEY = "CR-";
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
	}

	public void testEmptyStatusCollection() throws Exception {
		testedListener.updateReviews(new ArrayList<Review>());
		assertEquals(1, output.count);
        assertEquals(
                "<html>" + HtmlCrucibleStatusListener.BODY_WITH_STYLE + "No reviews at this time.</body></html>",
                output.htmlPage);
	}

	public void testSingleSuccessResult() throws Exception {
		Collection<Review> reviewInfo = new ArrayList<Review>();

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
			sb.append("<tr><td valign=\"top\"><b><font color=\"blue\"><a href=\"");
			sb.append(DEFAULT_SERVER_URL);
			sb.append("cru/");
			sb.append(DEFAULT_PROJECT_KEY);
			sb.append(i);
			sb.append("\">");
			sb.append(DEFAULT_PERM_ID + i);
			sb.append("</a></font></b></td>");
			sb.append("<td valign=\"top\">" + DEFAULT_REVIEW_NAME + "</td>");
			sb.append("<td valign=\"top\">" + DEFAULT_AUTHOR + i + "</td>");
			sb.append("<td valign=\"top\">" + DEFAULT_STATE.value() + "</td>");
			sb.append("<td valign=\"top\">");
			sb.append(DEFAULT_REVIEWER + i);
			sb.append("</td></tr>");

			assertEquals(sb.toString(), trimWhitespace(table.getRow(i + 2 - 1).asXml()));

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

	public static Review generateReviewDataInfo(final String suffix) {

		ReviewBean rd = new ReviewBean();
		rd.setAuthor(new UserBean(DEFAULT_AUTHOR + suffix));
		rd.setCreator(new UserBean(DEFAULT_CREATOR));
		rd.setDescription(DEFAULT_DESCRIPTION);
		rd.setModerator(new UserBean(DEFAULT_MODERATOR));
		rd.setName(DEFAULT_PROJECT_NAME);

		PermIdBean permId = new PermIdBean();
		permId.setId(DEFAULT_PERM_ID + suffix);
		rd.setPermId(permId);
		rd.setProjectKey(DEFAULT_PROJECT_KEY + suffix);
		rd.setState(DEFAULT_STATE);

		ArrayList reviewers = new ArrayList<String>();
        reviewers.add(new Reviewer() {
            public String getUserName() {
                return DEFAULT_REVIEW_NAME + suffix;
            }

            public String getDisplayName() {
                return "";
            }

            public int compareTo(User that) {
                return 0;
            }

            public boolean isCompleted() {
                return false;
            }
        });
		rd.setReviewers(reviewers);


		return rd;
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