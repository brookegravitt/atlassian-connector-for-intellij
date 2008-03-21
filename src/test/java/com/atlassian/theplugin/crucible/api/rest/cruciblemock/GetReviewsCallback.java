package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import com.atlassian.theplugin.crucible.api.State;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class GetReviewsCallback implements JettyMockServer.Callback {
	List<State> states;

	public GetReviewsCallback(List<State> states) {
		this.states = states;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1"));

		Document doc;
		final String[] statesParam = request.getParameterValues("state");
		if (statesParam != null) {
			assertTrue(1 == statesParam.length);
			String[] stateStrings = statesParam[0].split(",");
			List<State> returnStates = new ArrayList<State>();
			for (String stateString : stateStrings) {
				State s = State.fromValue(stateString);
				if (states.contains(s)) {
					returnStates.add(s);
				}
			}
			doc = getReviews(returnStates);
		} else {
			doc = getReviews(states);
		}
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, response.getOutputStream());
	}

	private Document getReviews(List<State> states) {
		Element root = new Element("reviews");
		Document doc = new Document(root);
		for (State state : states) {
			root.addContent(getReviewInState(state));
		}
		return doc;
	}

	private Element getReviewInState(State state) {
		Element reviewData = new Element("reviewData");

		addTag(reviewData, "author", "author");
		addTag(reviewData, "creator", "creator");
		addTag(reviewData, "description", "description");
		addTag(reviewData, "moderator", "moderator");
		addTag(reviewData, "name", "name");
		addTag(reviewData, "projectKey", "PR");
		addTag(reviewData, "repoName", "RepoName");
		addTag(reviewData, "state", state.value());

		Element newPermaId = new Element("permaId");
		Element newId = new Element("id");
		newId.addContent("PR-1");
		newPermaId.addContent(newId);
		reviewData.addContent(newPermaId);

		return reviewData;
	}

	void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		root.addContent(newElement);
	}
}