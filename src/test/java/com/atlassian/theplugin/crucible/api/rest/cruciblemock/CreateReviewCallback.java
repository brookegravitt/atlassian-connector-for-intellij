package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import com.atlassian.theplugin.crucible.api.PermIdBean;
import com.atlassian.theplugin.crucible.api.ReviewDataBean;
import com.atlassian.theplugin.crucible.api.State;
import com.atlassian.theplugin.crucible.api.rest.ReviewUtil;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class CreateReviewCallback implements JettyMockServer.Callback {
	public static final String REPO_NAME = "AtlassianSVN";
	public static final String PERM_ID = "PR-1";

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1"));
		assertTrue("POST".equalsIgnoreCase(request.getMethod()));

		SAXBuilder builder = new SAXBuilder();
		Document req = builder.build(request.getInputStream());
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		out.output(req, System.out);
		XPath xpath = XPath.newInstance("/createReview/reviewData");
		@SuppressWarnings("unchecked")
		List<Element> elements = xpath.selectNodes(req);

		ReviewDataBean reviewData = null;
		if (elements != null && !elements.isEmpty()) {
			reviewData = (ReviewDataBean) ReviewUtil.parseReviewNode(elements.iterator().next());
			reviewData.setState(State.DRAFT);
			PermIdBean permId = new PermIdBean();
			permId.setId(PERM_ID);
			reviewData.setPermaId(permId);
			reviewData.setRepoName(REPO_NAME);
		}

		Document doc = ReviewUtil.prepareReviewNode(reviewData);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, System.out);
		outputter.output(doc, response.getOutputStream());
	}
}
