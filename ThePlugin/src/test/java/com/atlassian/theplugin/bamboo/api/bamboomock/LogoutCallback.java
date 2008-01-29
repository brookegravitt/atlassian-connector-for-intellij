package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutCallback implements JettyMockServer.Callback {

	private final String expectedToken;

	public LogoutCallback() {
		this(LoginCallback.AUTH_TOKEN);
	}

	public LogoutCallback(String expectedToken) {
		this.expectedToken = expectedToken;
	}


	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/api/rest/logout.action"));

		final String[] authTokens = request.getParameterValues("auth");
		assertEquals(1, authTokens.length);

		final String authToken = authTokens[0];
		assertEquals(expectedToken, authToken);

		Util.copyResource(response.getOutputStream(), "logoutResponse.xml");
		response.getOutputStream().flush();

	}

}
