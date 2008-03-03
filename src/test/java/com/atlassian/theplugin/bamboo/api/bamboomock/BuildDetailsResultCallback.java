package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BuildDetailsResultCallback implements JettyMockServer.Callback {

	private final String resourcePrefix = "";
	private final String fileName;
	private final String buildNumber;

	public BuildDetailsResultCallback(String fileName, String buildNumber) {
		this.fileName = fileName;
		this.buildNumber = buildNumber;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/api/rest/getBuildResultsDetails.action"));

		final String[] authTokens = request.getParameterValues("auth");
		final String[] buildKeys = request.getParameterValues("buildKey");
		final String[] buildNumbers = request.getParameterValues("buildNumber");

		assertEquals(1, authTokens.length);
		assertEquals(1, buildKeys.length);
		assertEquals(1, buildNumbers.length);

		final String authToken = authTokens[0];
		final String buildKey = buildKeys[0];
		final String buildNumber = buildNumbers[0];

		assertEquals(LoginCallback.AUTH_TOKEN, authToken);
		assertEquals("TP-DEF", buildKey);
		assertEquals(this.buildNumber, buildNumber);

		Util.copyResource(response.getOutputStream(), resourcePrefix + fileName);
		response.getOutputStream().flush();

	}

}