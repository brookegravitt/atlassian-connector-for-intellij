package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import com.atlassian.theplugin.crucible.api.ReviewData;
import com.atlassian.theplugin.crucible.api.ReviewDataBean;
import com.atlassian.theplugin.crucible.api.State;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-22
 * Time: 19:01:52
 * To change this template use File | Settings | File Templates.
 */
public class CreateReviewCallback implements JettyMockServer.Callback {
	private ReviewDataBean reviewData;

	public CreateReviewCallback(ReviewDataBean review) {
		this.reviewData = review;
	}
	
	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1"));
		assertTrue("POST".equalsIgnoreCase(request.getMethod()));

		Document doc = getReview(reviewData);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, response.getOutputStream());
	}

	private Document getReview(ReviewData review) {
		Element reviewData = new Element("reviewData");
		Document doc = new Document(reviewData);
		addTag(reviewData, "author", review.getAuthor());
		addTag(reviewData, "creator", review.getCreator());
		addTag(reviewData, "description", review.getDescription());
		addTag(reviewData, "moderator", review.getModerator());
		addTag(reviewData, "name", review.getName());
		addTag(reviewData, "projectKey", review.getProjectKey());
		addTag(reviewData, "repoName", review.getRepoName());
		addTag(reviewData, "state", State.DRAFT.value());

		Element newPermaId = new Element("permaId");
		Element newId = new Element("id");
		newId.addContent("PR-1");
		newPermaId.addContent(newId);
		reviewData.addContent(newPermaId);

		return doc;
	}

	void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		root.addContent(newElement);
	}
}
