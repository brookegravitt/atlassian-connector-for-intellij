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

import com.atlassian.connector.intellij.util.HttpClientFactory;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.exception.IncorrectVersionException;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.Version;
import com.atlassian.theplugin.exception.VersionServiceException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.InvalidCredentialsException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.InputStream;

public final class InfoServer {

	private InfoServer() {
	}

	public static VersionInfo getLatestPluginVersion(final UsageStatisticsGenerator usageStatisticsGenerator,
			boolean checkUnstableVersions) throws VersionServiceException, IncorrectVersionException {

        String serviceUrl = getServiceUrl(checkUnstableVersions);
		return getLatestPluginVersion(serviceUrl, usageStatisticsGenerator);
	}

    private static String getServiceUrl(boolean checkUnstableVersions) {
        return checkUnstableVersions ? PluginUtil.LATEST_VERSION_INFO_URL : PluginUtil.STABLE_VERSION_INFO_URL;
    }

    protected static VersionInfo getLatestPluginVersion(final String serviceUrl,
			final UsageStatisticsGenerator usageStatisticsGenerator)
			throws VersionServiceException, IncorrectVersionException {

		String getMethodUrl = serviceUrl;
		try {
			HttpClient client;
			try {
				client = HttpClientFactory.getClient();
			} catch (HttpProxySettingsException e) {
				throw new VersionServiceException("Connection error while retrieving the latest plugin version.", e);
			}
			if (usageStatisticsGenerator != null) {
				getMethodUrl += "?" + usageStatisticsGenerator.getStatisticsUrlSuffix();
			}
			GetMethod method = new GetMethod(getMethodUrl);
			try {
				client.executeMethod(method);
			} catch (IllegalArgumentException e) {
				throw new VersionServiceException("Connection error while retrieving the latest plugin version.", e);
			}
			InputStream is = method.getResponseBodyAsStream();
			SAXBuilder builder = new SAXBuilder();
			builder.setValidation(false);
			Document doc = builder.build(is);
			return new VersionInfo(doc);
        } catch (InvalidCredentialsException e) {
            throw new VersionServiceException("Connection error while retrieving the latest plugin version.", e);
		} catch (IOException e) {
			throw new VersionServiceException("Connection error while retrieving the latest plugin version.", e);
		} catch (JDOMException e) {
			throw new VersionServiceException(
					"Error while parsing xml response from version service server at " + getMethodUrl, e);
		}
	}

    public static void reportOptInOptOut(final long uid, final Boolean optIn) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                String getMethodUrl = getServiceUrl(false);
                HttpClient client;
                try {
                    client = HttpClientFactory.getClient();
                    getMethodUrl += "?uid=" + uid + "&userOptedIn=" + (optIn == null || optIn ? 1 : 0);
                    GetMethod method = new GetMethod(getMethodUrl);
                    client.executeMethod(method);
                    method.getResponseBodyAsStream(); // ignore response
                } catch (InvalidCredentialsException e) {
//                    LoggerImpl.getInstance().info(e.getMessage());
                } catch (HttpProxySettingsException e) {
                    LoggerImpl.getInstance().error(e);
                } catch (IOException e) {
                    LoggerImpl.getInstance().error(e);
                }
            }
        });
        t.start();
    }

    public static class VersionInfo {
		public enum Type {
			STABLE,
			UNSTABLE
		}
		private final Version version;

		private final String downloadUrl;
		private final String releaseNotes;
		private final String releaseNotesUrl;

		/**
		 * Only for internal use (package scope)
		 * @param doc
		 * @throws VersionServiceException
		 * @throws com.atlassian.theplugin.commons.exception.IncorrectVersionException
		 */
		public VersionInfo(Document doc) throws VersionServiceException, IncorrectVersionException {
			version = new Version(getValue("/response/version/number", doc));
			downloadUrl = StringUtils.trim(getValue("/response/version/downloadUrl", doc));
			releaseNotes = getValue("/response/version/releaseNotes", doc);
			releaseNotesUrl = StringUtils.trim(getValue("/response/version/releaseNotesUrl", doc));
		}

		/**
		 * Only for test use
		 * @param version
		 * @param downloadUrl
		 */
		public VersionInfo(Version version, String downloadUrl) {
			this.version = version;
			this.downloadUrl = downloadUrl;
			this.releaseNotes = null;
			this.releaseNotesUrl = null;
		}

		private String getValue(String path, Document doc) throws VersionServiceException {
			XPath xpath;
			Element element;
			try {
				xpath = XPath.newInstance(path);
				element = (Element) xpath.selectSingleNode(doc);
				if (element == null) {
					throw new VersionServiceException("Error while parsing metadata file");
				}
			} catch (JDOMException e) {
				throw new VersionServiceException("Error while parsing metadata file", e);
			}
			return element.getValue();
		}

		public Version getVersion() {
			return version;
		}

		public String getDownloadUrl() {
			return downloadUrl;
		}

		public String getReleaseNotes() {
			return releaseNotes;
		}

		public String getReleaseNotesUrl() {
			return releaseNotesUrl;
		}
	}
}
