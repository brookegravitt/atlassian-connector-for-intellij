package com.atlassian.theplugin.util;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-23
 * Time: 11:56:09
 * To change this template use File | Settings | File Templates.
 */
public final class HttpConnectionFactory {
	private HttpConnectionFactory() {
	}

	private static SSLSocketFactory socketFactory = getSSLSocketFactory();
	private static HostnameVerifier hostnameVerifier = new EasyHostnameVerifier();

	public static URLConnection getConnection(String urlString) throws IOException {
		URL url = new URL(urlString);
		return getConnection(url);
	}

	public static URLConnection getConnection(URL url) throws IOException {
		URLConnection c = url.openConnection();
		if (c instanceof HttpsURLConnection) {
			HttpsURLConnection cs = (HttpsURLConnection) c;
			cs.setSSLSocketFactory(socketFactory);
			cs.setHostnameVerifier(hostnameVerifier);
		}
		return c;
	}

	private static SSLSocketFactory getSSLSocketFactory() {
		TrustManager[] trustAllCerts = new TrustManager[]{
				new EasyTrustManager()
		};
		SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return sc.getSocketFactory();
	}
}
