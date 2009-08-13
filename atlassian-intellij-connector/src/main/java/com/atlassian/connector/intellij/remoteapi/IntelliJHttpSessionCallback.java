package com.atlassian.connector.intellij.remoteapi;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.intellij.util.HttpClientFactory;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.idea.BugReporting;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * @author Wojciech Seliga
 */
public class IntelliJHttpSessionCallback extends HttpSessionCallbackImpl {
	private static final String USER_AGENT = "Atlassian Connector for IntelliJ/" + BugReporting.getVersionString();

	public HttpClient getHttpClient(ConnectionCfg server) throws HttpProxySettingsException {
		final HttpClient client = HttpClientFactory.getClient();
		client.getParams().setParameter(HttpMethodParams.USER_AGENT, USER_AGENT);
		return client;
	}

}
