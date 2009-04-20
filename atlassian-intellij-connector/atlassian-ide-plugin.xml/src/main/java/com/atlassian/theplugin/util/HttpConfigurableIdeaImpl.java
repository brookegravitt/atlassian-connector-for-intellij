/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;
import com.intellij.util.net.HTTPProxySettingsDialog;
import com.intellij.util.net.HttpConfigurable;

public final class HttpConfigurableIdeaImpl implements HttpConfigurableAdapter {

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