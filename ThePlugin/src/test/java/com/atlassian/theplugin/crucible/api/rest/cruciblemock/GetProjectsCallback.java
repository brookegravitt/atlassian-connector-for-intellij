package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetProjectsCallback implements JettyMockServer.Callback {
	private int size;

	public GetProjectsCallback(int size) {
		this.size = size;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/rest-service/projects-v1"));

		Document doc;
		doc = getProjects();
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, response.getOutputStream());
	}

	private Document getProjects() {
		Element root = new Element("projects");
		Document doc = new Document(root);
		for (int i = 0; i < size; i++) {
			root.addContent(getProject(i));
		}
		return doc;
	}

	private Element getProject(int i) {
		Element projectData = new Element("projectData");

		addTag(projectData, "allowReviewersToJoin", "false");
		addTag(projectData, "id", Integer.toString(i));
		addTag(projectData, "key", "CR" + Integer.toString(i));
		addTag(projectData, "name", "ProjectName" + Integer.toString(i));
		addTag(projectData, "permissionSchemeId", "1");

		return projectData;
	}

	void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		root.addContent(newElement);
	}
}