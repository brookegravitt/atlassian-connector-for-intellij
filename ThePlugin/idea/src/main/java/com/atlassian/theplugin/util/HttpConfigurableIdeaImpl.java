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
	private HttpConfigurableIdeaImpl(){}
	private static HttpConfigurableIdeaImpl instance;

	public static HttpConfigurableIdeaImpl getInstance(){
		if (instance == null){
			instance = new HttpConfigurableIdeaImpl();
		}

		return instance;
	}


	public boolean isKeepProxyPassowrd() {
		return HttpConfigurable.getInstance().KEEP_PROXY_PASSWORD;
	}

	public boolean isProxyAuthentication() {
		return HttpConfigurable.getInstance().PROXY_AUTHENTICATION;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public boolean isUseHttpProxy() {
		return HttpConfigurable.getInstance().USE_HTTP_PROXY;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getPlainProxyPassword() {
		return HttpConfigurable.getInstance().getPlainProxyPassword();  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getProxyLogin() {
		return HttpConfigurable.getInstance().PROXY_LOGIN;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public int getProxyPort() {
		return HttpConfigurable.getInstance().PROXY_PORT;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getProxyHost() {
		return HttpConfigurable.getInstance().PROXY_HOST;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public Object getHTTPProxySettingsDialog() {
		return new HTTPProxySettingsDialog();  //To change body of implemented methods use File | Settings | File Templates.
	}
}