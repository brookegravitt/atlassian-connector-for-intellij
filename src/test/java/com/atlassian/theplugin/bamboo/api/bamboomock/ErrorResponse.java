package com.atlassian.theplugin.bamboo.api.bamboomock;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorResponse implements JettyMockServer.Callback {
	private final int errorCode;

	private final static String ERROR_PREFIX = "HTTP ";
	private final static String ERROR_MESSAGE = "error text";
	private String errorDescription;

	public ErrorResponse(int error, String errorDescription) {
		this.errorCode = error;
		this.errorDescription = errorDescription;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.sendError(errorCode, ERROR_MESSAGE);
	}

	public String getErrorMessage() {
		return ERROR_PREFIX + errorCode + " (" + errorDescription + ")" + "\n" + ERROR_MESSAGE;
	}

	public static String getStaticErrorMessage(int error, String errorDesc) {
		return ERROR_PREFIX + error + " (" + errorDesc + ")\n" + ERROR_MESSAGE;
	}
}
