package com.atlassian.theplugin.commons.util;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: May 6, 2008
 * Time: 12:49:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface HttpConfigurableAdapter {

	boolean isKeepProxyPassowrd();
	boolean isProxyAuthentication();
	boolean isUseHttpProxy();
	String getPlainProxyPassword();
	String getProxyLogin();
	int	getProxyPort();
	String getProxyHost();
	Object getHTTPProxySettingsDialog();

}
