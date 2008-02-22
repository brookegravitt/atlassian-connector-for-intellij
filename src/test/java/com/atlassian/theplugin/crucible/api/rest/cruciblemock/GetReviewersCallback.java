package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetReviewersCallback implements JettyMockServer.Callback {
	String[] reviewers;
	private boolean reviewExists;

	public GetReviewersCallback(String[] reviewers) {
		this(reviewers, true);
	}

	public GetReviewersCallback(String[] reviewers, boolean reviewExists) {
		this.reviewExists = reviewExists;
		this.reviewers = reviewers;
	}


	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1/PR-1/reviewers"));

		Document doc;
		if (reviewExists) {
			doc = getReviewers(reviewers);
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			outputter.output(doc, response.getOutputStream());
		} else {
			response.sendError(500, "FishEye was unable to process your request");
		}

	}

	private Document getReviewers(String[] reviewers) {
		Element root = new Element("reviewers");
		Document doc = new Document(root);
		for (String reviewer : reviewers) {
			addTag(root, "reviewer", reviewer);
		}
		return doc;
	}

	void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		root.addContent(newElement);
	}
}