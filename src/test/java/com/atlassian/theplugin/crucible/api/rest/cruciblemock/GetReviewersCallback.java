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

		assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1/PR-1/reviewers"));

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
		root.getContent().add(newElement);
	}
}