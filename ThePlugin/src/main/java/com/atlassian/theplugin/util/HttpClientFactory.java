package com.atlassian.theplugin.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import thirdparty.apache.EasySSLProtocolSocketFactory;

public final class HttpClientFactory {
	private static MultiThreadedHttpConnectionManager connectionManager;

	static {
		Protocol.registerProtocol("https", new Protocol(
				"https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), EasySSLProtocolSocketFactory.SSL_PORT));
		connectionManager = new MultiThreadedHttpConnectionManager();
	}

	private HttpClientFactory() {		
	}

	public static HttpClient getClient() {
		return new HttpClient(connectionManager);
	}

	public static MultiThreadedHttpConnectionManager getConnectionManager() {
		return connectionManager;
	}
}
