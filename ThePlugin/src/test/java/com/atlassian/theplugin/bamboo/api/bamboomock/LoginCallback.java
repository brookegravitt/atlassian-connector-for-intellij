package com.atlassian.theplugin.bamboo.api.bamboomock;

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


		assertTrue(request.getPathInfo().endsWith("/api/api/login.action"));

		final String[] usernames = request.getParameterValues("username");
		final String[] passwords = request.getParameterValues("password");
		final String[] os_usernames = request.getParameterValues("os_username");
		final String[] os_passwords = request.getParameterValues("os_password");

		assertEquals(1, usernames.length);
		assertEquals(1, os_usernames.length);
		assertEquals(1, passwords.length);
		assertEquals(1, os_passwords.length);

		final String username = usernames[0];
		final String os_username = os_usernames[0];
		final String password = passwords[0];
		final String os_password = os_passwords[0];

		assertNotNull(username);
		assertNotNull(os_username);
		assertNotNull(password);
		assertNotNull(os_password);

		assertEquals(username, os_username);
		assertEquals(password, os_password);

		assertEquals(expectedUsername, username);
		assertEquals(expectedPassword, password);

		Util.copyResource(response.getOutputStream(), fail ? "loginFailedResponse.xml" : "loginSuccessResponse.xml");
		response.getOutputStream().flush();

	}


}
