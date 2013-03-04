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

package com.atlassian.connector.intellij.util;

import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public final class HttpClientFactory {
	private static MultiThreadedHttpConnectionManager connectionManager;

	private static final int CONNECTION_MANAGER_TIMEOUT = 120000;
	private static final int CONNECTION_TIMEOUT = 120000;
	private static final int DATA_TIMOUT = 120000;

	private static final int TOTAL_MAX_CONNECTIONS = 50;

	private static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 3;
	private static int dataTimeout = DATA_TIMOUT;
	private static int connectionTimeout = CONNECTION_TIMEOUT;
	private static int connectionManagerTimeout = CONNECTION_MANAGER_TIMEOUT;

	static {
		connectionManager =	new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setConnectionTimeout(getConnectionTimeout());
		connectionManager.getParams().setMaxTotalConnections(TOTAL_MAX_CONNECTIONS);
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(DEFAULT_MAX_CONNECTIONS_PER_HOST);
	}

	///CLOVER:OFF
	private HttpClientFactory() {

	}


	public static void setDataTimeout(int dataTimeout) {
		HttpClientFactory.dataTimeout = dataTimeout;
	}

	protected static void setConnectionTimeout(int connectionTimeout) {
		HttpClientFactory.connectionTimeout = connectionTimeout;
	}

	protected static void setConnectionManagerTimeout(int connectionManagerTimeout) {
		HttpClientFactory.connectionManagerTimeout = connectionManagerTimeout;
	}
///CLOVER:ON

	public static HttpClient getClient() throws HttpProxySettingsException {
		HttpClient httpClient = new HttpClient(connectionManager);
		httpClient.getParams().setConnectionManagerTimeout(getConnectionManagerTimeout());
		httpClient.getParams().setSoTimeout(getDataTimeout());
		HttpConfigurableAdapter httpConfigurableAdapter =
				ConfigurationFactory.getConfiguration().transientGetHttpConfigurable();
		boolean useIdeaProxySettings =
				ConfigurationFactory.getConfiguration().getGeneralConfigurationData().getUseIdeaProxySettings();
		if (useIdeaProxySettings && (httpConfigurableAdapter != null)) {
			if (httpConfigurableAdapter.isUseHttpProxy()) {
				httpClient.getHostConfiguration().setProxy(httpConfigurableAdapter.getProxyHost(),
						httpConfigurableAdapter.getProxyPort());
				if (httpConfigurableAdapter.isProxyAuthentication()) {

					if (httpConfigurableAdapter.getPlainProxyPassword().length() == 0
							&& !httpConfigurableAdapter.isKeepProxyPassowrd()) { //ask user for proxy passowrd

						throw new HttpProxySettingsException("HTTP Proxy password is incorrect");
					}

					Credentials creds = null;

					//
					// code below stolen from AXIS: /transport/http/CommonsHTTPSender.java
					//
					String proxyUser = httpConfigurableAdapter.getProxyLogin();
					int domainIndex = proxyUser.indexOf("\\");
					if (domainIndex > 0) {
						// if the username is in the form "user\domain"
						// then use NTCredentials instead of UsernamePasswordCredentials
						String domain = proxyUser.substring(0, domainIndex);
						if (proxyUser.length() > domainIndex + 1) {
							String user = proxyUser.substring(domainIndex + 1);
							creds = new NTCredentials(user,	httpConfigurableAdapter.getPlainProxyPassword(),
											httpConfigurableAdapter.getProxyHost(), domain);
						}
					} else {
						creds = new UsernamePasswordCredentials(proxyUser, httpConfigurableAdapter.getPlainProxyPassword());
					}
					//
					// end of code stolen from AXIS
					//

					httpClient.getState().setProxyCredentials(
							new AuthScope(httpConfigurableAdapter.getProxyHost(), httpConfigurableAdapter.getProxyPort()),
							creds);
				}
			}
		}

		return httpClient;
	}


	private static int getConnectionManagerTimeout() {
		return connectionManagerTimeout;
	}

	private static int getDataTimeout() {
		return dataTimeout;
	}

	private static int getConnectionTimeout() {
		return connectionTimeout;
	}


	public static MultiThreadedHttpConnectionManager getConnectionManager() {
		return connectionManager;
	}

}
