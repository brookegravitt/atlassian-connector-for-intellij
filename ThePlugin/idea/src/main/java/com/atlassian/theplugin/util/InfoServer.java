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

import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.commons.exception.IncorrectVersionException;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.util.Version;
import com.atlassian.theplugin.commons.util.HttpClientFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;

public final class InfoServer {

	private InfoServer() {
	}

	public static VersionInfo getLatestPluginVersion(long uid,
													 boolean checkUnstableVersions)
			throws VersionServiceException, IncorrectVersionException {

		String serviceUrl = PluginUtil.STABLE_VERSION_INFO_URL;
		if (checkUnstableVersions) {
			serviceUrl = PluginUtil.LATEST_VERSION_INFO_URL;
		}
		return getLatestPluginVersion(serviceUrl, uid, checkUnstableVersions);
	}

	protected static VersionInfo getLatestPluginVersion(String serviceUrl, long uid, boolean checkUnstableVersions) 
			throws VersionServiceException, IncorrectVersionException {
		try {

			HttpClient client = null;
			try {
				client = HttpClientFactory.getClient();
			} catch (HttpProxySettingsException e) {
				throw new VersionServiceException("Connection error while retrieving the latest plugin version.", e);
			}
			GetMethod method = new GetMethod(serviceUrl + "?uid=" + uid);
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
		} catch (IOException e) {
			throw new VersionServiceException("Connection error while retrieving the latest plugin version.", e);
		} catch (JDOMException e) {
			throw new VersionServiceException(
					"Error while parsing xml response from version service server at "
					+ serviceUrl
					+ "?uid="
					+ uid, e);
		}
	}


	public static class VersionInfo {
		public enum Type {
			STABLE,
			UNSTABLE
		}
		private Version version;

		private String downloadUrl;
		private String releaseNotes;
		private String releaseNotesUrl;

		/**
		 * Only for internal use (package scope)
		 * @param doc
		 * @throws VersionServiceException
		 * @throws com.atlassian.theplugin.commons.exception.IncorrectVersionException
		 */
		VersionInfo(Document doc) throws VersionServiceException, IncorrectVersionException {
			version = new Version(getValue("/response/version/number", doc));
			downloadUrl = getValue("/response/version/downloadUrl", doc);
			releaseNotesUrl = getValue("/response/version/releaseNotes", doc);
			final String urlString = getValue("/response/version/releaseNotesUrl", doc);			
		}

		/**
		 * Only for test use
		 * @param version
		 * @param downloadUrl
		 */
		public VersionInfo(Version version, String downloadUrl) {
			this.version = version;
			this.downloadUrl = downloadUrl;
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

		public URL getReleaseNotesUrl() {
			return releaseNotesUrl;
		}
	}
}
