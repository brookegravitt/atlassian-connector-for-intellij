package com.atlassian.theplugin.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import thirdparty.apache.EasySSLProtocolSocketFactory;

public final class HttpClientFactory {
	private static MultiThreadedHttpConnectionManager connectionManager;

	private static final int CONNECTION_TIMOUT = 20000;

	static {
		Protocol.registerProtocol("https", new Protocol(
				"https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), EasySSLProtocolSocketFactory.SSL_PORT));
		connectionManager =	new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setConnectionTimeout(CONNECTION_TIMOUT);
	}

	///CLOVER:OFF
	private HttpClientFactory() {		
	}
	///CLOVER:ON

	public static HttpClient getClient() {
		return new HttpClient(connectionManager);
	}

	public static MultiThreadedHttpConnectionManager getConnectionManager() {
		return connectionManager;
	}
}
