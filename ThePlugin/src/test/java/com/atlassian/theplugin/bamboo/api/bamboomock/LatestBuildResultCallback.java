package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LatestBuildResultCallback implements JettyMockServer.Callback {

	private final String resourcePrefix;

	public LatestBuildResultCallback() {
		resourcePrefix = "";
	}

	public LatestBuildResultCallback(String resourcePrefix) {
		this.resourcePrefix = resourcePrefix + "-";
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/api/api/getLatestBuildResults.action"));

		final String[] authTokens = request.getParameterValues("auth");
		final String[] buildKeys = request.getParameterValues("buildKey");

		assertEquals(1, authTokens.length);
		assertEquals(1, buildKeys.length);

		final String authToken = authTokens[0];
		final String buildKey = buildKeys[0];

		assertEquals(LoginCallback.AUTH_TOKEN, authToken);
		assertEquals("TP-DEF", buildKey);

		Util.copyResource(response.getOutputStream(), resourcePrefix + "latestBuildResultResponse.xml");
		response.getOutputStream().flush();

	}

}
