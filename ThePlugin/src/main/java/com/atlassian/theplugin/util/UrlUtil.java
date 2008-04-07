package com.atlassian.theplugin.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-27
 * Time: 14:20:49
 * To change this template use File | Settings | File Templates.
 */
public abstract class UrlUtil {

	private UrlUtil() { }

	public static String addHttpPrefix(String address) {
		if (address == null) {
			return null;
		} else if (address.trim().length() == 0) {
			return address;
		} else if (!(address.trim().startsWith("http://") || address.trim().startsWith("https://"))) {
			return "http://" + address.trim();
		} else {
			return address;
		}
	}

	public static String removeUrlTrailingSlashes(String address) {
		if (address == null) {
			return null;
		}
		try {
			URL url = new URL(address);
			if (url.getHost().length() == 0) {
				return address;
			}
		} catch (MalformedURLException e) {
			return address;
		}

		while (address.endsWith("/")) {
			address = address.substring(0, address.length() - 1);
		}
		return address;
	}


	public static void validateUrl(String urlString) throws MalformedURLException {

		if (urlString == null || urlString.length() == 0) {
			throw new MalformedURLException("Malformed URL: null or empty");
		}

		try {
			URL url = new URL(urlString);

			// check the host name
			if (url.getHost().length() == 0) {
				throw new MalformedURLException("Url must contain valid host.");
			}
			// check the port number
			if (url.getPort() >= 2 * Short.MAX_VALUE) {
				throw new MalformedURLException("Url port invalid");
			}
		} catch (MalformedURLException e) {
			throw new MalformedURLException("Malformed URL: " + e.getMessage());
		}
	}

}
