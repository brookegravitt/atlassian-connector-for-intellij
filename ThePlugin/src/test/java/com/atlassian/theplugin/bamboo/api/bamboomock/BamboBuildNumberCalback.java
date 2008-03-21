package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Mar 11, 2008
 * Time: 9:11:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class BamboBuildNumberCalback implements JettyMockServer.Callback {
	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		assertTrue(request.getPathInfo().endsWith("/api/rest/getBambooBuildNumber.action"));

		final String[] authTokens = request.getParameterValues("auth");

		assertEquals(1, authTokens.length);

		final String authToken = authTokens[0];

		assertEquals(LoginCallback.AUTH_TOKEN, authToken);
		Util.copyResource(response.getOutputStream(), "bambooBuildNumberResponse.xml");
		response.getOutputStream().flush();
	}
}
