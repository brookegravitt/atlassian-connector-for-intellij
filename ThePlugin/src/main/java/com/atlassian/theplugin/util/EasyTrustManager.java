package com.atlassian.theplugin.util;

import javax.net.ssl.X509TrustManager;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-23
 * Time: 14:43:06
 * To change this template use File | Settings | File Templates.
 */
public class EasyTrustManager implements X509TrustManager {
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public void checkClientTrusted(
			java.security.cert.X509Certificate[] certs, String authType) {
	}

	public void checkServerTrusted(
			java.security.cert.X509Certificate[] certs, String authType) {
	}
}
