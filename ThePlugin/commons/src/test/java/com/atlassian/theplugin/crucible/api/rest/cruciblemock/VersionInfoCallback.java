package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static junit.framework.Assert.assertTrue;

public class VersionInfoCallback implements JettyMockServer.Callback {
	private boolean cru16;

	public VersionInfoCallback(boolean cru16) {
		this.cru16 = cru16;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1/versionInfo"));

		if (cru16) {
			Util.copyResource(response.getOutputStream(), "versionInfoSuccessResponse.xml");
		} else {
			Util.copyResource(response.getOutputStream(), "versionInfoFailureResponse.xml");
			response.setStatus(500);
		}
		response.getOutputStream().flush();

	}
}
