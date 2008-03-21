package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FavouritePlanListCallback implements JettyMockServer.Callback {
	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/remoteapi/remoteapi/getLatestUserBuilds.action"));

		final String[] authTokens = request.getParameterValues("auth");
		assertEquals(1, authTokens.length);

		final String authToken = authTokens[0];
		assertEquals(LoginCallback.AUTH_TOKEN, authToken);

		Util.copyResource(response.getOutputStream(), "favouritePlansResponse.xml");
		response.getOutputStream().flush();

	}
}