/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddCommentToBuildCallback implements JettyMockServer.Callback {

	private final String resourcePrefix = "";
	private String buildNumber = "100";
	private String comment;
	private int errorReason = NON_ERROR;
	public static final int NON_ERROR = 0;
	public static final int NON_EXIST_FAIL = 1;


	public AddCommentToBuildCallback(String comment) {
		this.comment = comment;
	}

	public AddCommentToBuildCallback(String comment, String buildNumber, int reason) {
		this.buildNumber = buildNumber;
		this.errorReason = reason;
		this.comment = comment;
	}


	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/api/rest/addCommentToBuildResults.action"));

		final String[] authTokens = request.getParameterValues("auth");
		final String[] buildKeys = request.getParameterValues("buildKey");
		final String[] buildNumbers = request.getParameterValues("buildNumber");
		final String[] buildComments = request.getParameterValues("content");

		assertEquals(1, authTokens.length);
		assertEquals(1, buildKeys.length);
		assertEquals(1, buildNumbers.length);
		assertEquals(1, buildComments.length);

		final String authToken = authTokens[0];
		final String buildKey = buildKeys[0];
		final String buildNumber = buildNumbers[0];
		final String buildComment = buildComments[0];

		assertEquals(LoginCallback.AUTH_TOKEN, authToken);
		assertEquals("TP-DEF", buildKey);
		assertEquals(this.buildNumber, buildNumber);
		assertEquals(comment, buildComment);

		switch (errorReason) {
			case NON_ERROR:
				Util.copyResource(response.getOutputStream(), resourcePrefix + "emptyResponse.xml");
				break;
			case NON_EXIST_FAIL:
				Util.copyResource(response.getOutputStream(), resourcePrefix + "buildNotExistsResponse.xml");
				break;
		}
		response.getOutputStream().flush();

	}

}