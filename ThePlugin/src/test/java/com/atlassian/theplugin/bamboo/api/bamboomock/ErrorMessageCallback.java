package com.atlassian.theplugin.bamboo.api.bamboomock;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorMessageCallback implements JettyMockServer.Callback {

	private String responseFileName;

	public ErrorMessageCallback(String responseFileName) {
		this.responseFileName = responseFileName;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Util.copyResource(response.getOutputStream(), responseFileName);
		response.getOutputStream().flush();
	}


}