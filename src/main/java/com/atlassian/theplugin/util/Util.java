package com.atlassian.theplugin.util;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-27
 * Time: 14:20:49
 * To change this template use File | Settings | File Templates.
 */
public abstract class Util {

	private Util() { }
	
	public static String addHttpPrefix(String address) {
		if (address == null) {
			return null;
		} else if (address.length() == 0) {
			return address;
		} else if (!(address.startsWith("http://") || address.startsWith("https://"))) {
			return "http://" + address;
		} else {
			return address;
		}
	}
}
