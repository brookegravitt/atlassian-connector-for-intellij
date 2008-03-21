package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExecuteBuildCallback implements JettyMockServer.Callback {

	private final String resourcePrefix = "";
	private int errorReason = NON_ERROR;
	public static final int NON_ERROR = 0;
	public static final int NON_EXIST_FAIL = 1;

	public ExecuteBuildCallback() {
	}

	public ExecuteBuildCallback(int reason) {

		this.errorReason = reason;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/remoteapi/remoteapi/executeBuild.action"));

		final String[] authTokens = request.getParameterValues("auth");
		final String[] buildKeys = request.getParameterValues("buildKey");

		assertEquals(1, authTokens.length);
		assertEquals(1, buildKeys.length);

		final String authToken = authTokens[0];
		final String buildKey = buildKeys[0];

		assertEquals(LoginCallback.AUTH_TOKEN, authToken);
		assertEquals("TP-DEF", buildKey);

		switch (errorReason) {
			case NON_ERROR:
				Util.copyResource(response.getOutputStream(), resourcePrefix + "buildExecutedResponse.xml");
				break;
			case NON_EXIST_FAIL:
				Util.copyResource(response.getOutputStream(), resourcePrefix + "buildNotExecutedResponse.xml");
				break;
		}
		response.getOutputStream().flush();
	}

}