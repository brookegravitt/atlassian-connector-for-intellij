package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import static junit.framework.Assert.*;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginCallback implements JettyMockServer.Callback {

	private final String expectedUsername;
	private final String expectedPassword;
	private final boolean fail;

	public static final boolean ALWAYS_FAIL = true;
	public static final String AUTH_TOKEN = "authtokenstring";

	public LoginCallback(String expectedUsername, String expectedPassword) {
		this(expectedUsername, expectedPassword, false);
	}

	public LoginCallback(String expectedUsername, String expectedPassword, boolean alwaysFail) {
		this.expectedUsername = expectedUsername;
		this.expectedPassword = expectedPassword;

		fail = alwaysFail;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {


		assertTrue(request.getPathInfo().endsWith("/rest-service/auth-v1/login"));

		final String[] usernames = request.getParameterValues("userName");
		final String[] passwords = request.getParameterValues("password");

		assertEquals(1, usernames.length);
		assertEquals(1, passwords.length);

		final String username = usernames[0];
		final String password = passwords[0];

		assertNotNull(username);
		assertNotNull(password);

		assertEquals(expectedUsername, username);
		assertEquals(expectedPassword, password);

		Util.copyResource(response.getOutputStream(), fail ? "loginFailedResponse.xml" : "loginSuccessResponse.xml");
		response.getOutputStream().flush();

	}


}