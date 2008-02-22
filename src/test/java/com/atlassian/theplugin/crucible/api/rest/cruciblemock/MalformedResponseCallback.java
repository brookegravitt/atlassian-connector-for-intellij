package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MalformedResponseCallback implements JettyMockServer.Callback {
	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.getOutputStream().write("<tag></badtag>".getBytes());
		response.getOutputStream().flush();
	}
}