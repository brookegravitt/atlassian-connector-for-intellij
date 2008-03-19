package com.atlassian.theplugin.bamboo.api.bamboomock;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorResponse implements JettyMockServer.Callback {
	private final int errorCode;

	private final static String ERROR_PREFIX = "HTTP status code ";
	private final static String ERROR_MESSAGE = "error text";

	public ErrorResponse(int error) {
		this.errorCode = error;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.sendError(errorCode, ERROR_MESSAGE);
	}

	public String getErrorMessage() {
		return ERROR_PREFIX + errorCode + ": " + ERROR_MESSAGE;
	}

	public static String getStaticErrorMessage(int error) {
		return ERROR_PREFIX + error + ": " + ERROR_MESSAGE;
	}
}
