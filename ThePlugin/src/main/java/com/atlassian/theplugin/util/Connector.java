package com.atlassian.theplugin.util;

import com.atlassian.theplugin.exception.ThePluginException;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Mar 17, 2008
* Time: 4:46:50 PM
* To change this template use File | Settings | File Templates.
*/
public abstract class Connector {

	private String url;
	private String username;
	private String password;

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUserName(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public abstract void connect() throws ThePluginException;

	protected void validate() throws IllegalArgumentException {
		if (url == null) {
			throw new IllegalArgumentException("Url not provided.");
		}
		if (username == null) {
			throw new IllegalArgumentException("Username not provided.");
		}
		if (password == null) {
			throw new IllegalArgumentException("Password not provided.");
		}
	}

	public String getUrl() {
		return url;
	}

	public String getUserName() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
