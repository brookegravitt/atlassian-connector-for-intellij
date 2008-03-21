package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetReviewersCallback implements JettyMockServer.Callback {
	private String[] reviewers;

	public GetReviewersCallback(String[] reviewers) {
		this.reviewers = reviewers;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/api-service/reviews-v1/PR-1/reviewers"));

		Document doc = getReviewers(reviewers);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, response.getOutputStream());
	}

	private static Document getReviewers(String[] reviewers) {
		Element root = new Element("reviewers");
		Document doc = new Document(root);
		for (String reviewer : reviewers) {
			addTag(root, "reviewer", reviewer);
		}
		return doc;
	}

	private static void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		root.addContent(newElement);
	}
}