package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.HTTPProxySettingsDialog;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: May 6, 2008
 * Time: 3:39:42 PM
 * To change this template use File | Settings | File Templates.
 */


public class HttpConfigurableIdeaImpl implements HttpConfigurableAdapter {

	private static HttpConfigurableIdeaImpl instance;

	private HttpConfigurableIdeaImpl() {
	}

	public static HttpConfigurableIdeaImpl getInstance() {
		if (instance == null) {
			instance = new HttpConfigurableIdeaImpl();
		}

		return instance;
	}


	public boolean isKeepProxyPassowrd() {
		return HttpConfigurable.getInstance().KEEP_PROXY_PASSWORD;
	}

	public boolean isProxyAuthentication() {
		return HttpConfigurable.getInstance().PROXY_AUTHENTICATION;
	}

	public boolean isUseHttpProxy() {
		return HttpConfigurable.getInstance().USE_HTTP_PROXY;
	}

	public String getPlainProxyPassword() {
		return HttpConfigurable.getInstance().getPlainProxyPassword();
	}

	public String getProxyLogin() {
		return HttpConfigurable.getInstance().PROXY_LOGIN;
	}

	public int getProxyPort() {
		return HttpConfigurable.getInstance().PROXY_PORT;
	}

	public String getProxyHost() {
		return HttpConfigurable.getInstance().PROXY_HOST;
	}

	public Object getHTTPProxySettingsDialog() {
		return new HTTPProxySettingsDialog();
	}
}