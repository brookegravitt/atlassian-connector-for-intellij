package com.atlassian.theplugin.util;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Handle https connections without verifying the certificates.
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
		if (c instanceof HttpURLConnection) {
			// both Http and Https will match here
			if (url.getHost().length() == 0) {
				throw new MalformedURLException("Url must contain valid host.");
			}
		}
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
