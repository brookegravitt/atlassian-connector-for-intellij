package com.atlassian.theplugin.bamboo.api.bamboomock;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorResponse implements JettyMockServer.Callback {
	private final int errorCode;

	private final static String ERROR_MESSAGE = "error text";

	public ErrorResponse(int error) {
		this.errorCode = error;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.sendError(errorCode, ERROR_MESSAGE);
	}

	public static String getErrorMessage() {
		return ERROR_MESSAGE;
	}
}
