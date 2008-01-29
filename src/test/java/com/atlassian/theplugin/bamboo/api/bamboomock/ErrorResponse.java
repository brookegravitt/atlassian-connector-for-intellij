package com.atlassian.theplugin.bamboo.api.bamboomock;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorResponse implements JettyMockServer.Callback {
	private final int errorCode;

	public ErrorResponse(int error) {
		this.errorCode = error;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.sendError(errorCode, "error text");
	}
}
