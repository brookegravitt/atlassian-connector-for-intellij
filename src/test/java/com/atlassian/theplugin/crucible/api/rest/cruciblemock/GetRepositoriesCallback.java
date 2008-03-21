package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetRepositoriesCallback implements JettyMockServer.Callback {
	private int size;

	public GetRepositoriesCallback(int size) {
		this.size = size;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/remoteapi-service/repositories-v1"));

		Document doc;
		doc = getRepositories();
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, response.getOutputStream());
	}

	private Document getRepositories() {
		Element root = new Element("repositories");
		Document doc = new Document(root);
		for (int i = 0; i < size; i++) {
			root.addContent(getRepositories(i));
		}
		return doc;
	}

	private Element getRepositories(int i) {
		Element projectData = new Element("repoData");
		addTag(projectData, "name", "RepoName" + Integer.toString(i));
		return projectData;
	}

	void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		root.addContent(newElement);
	}
}