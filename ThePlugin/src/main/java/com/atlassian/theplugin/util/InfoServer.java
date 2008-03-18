package com.atlassian.theplugin.util;

import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.exception.IncorrectVersionException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
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

	public static VersionInfo getLatestPluginVersion(String serviceUrl, long uid)
			throws VersionServiceException, IncorrectVersionException {

		try {
			HttpClient client = new HttpClient();
			GetMethod method = new GetMethod(serviceUrl + "?uid=" + uid);
			try {
				client.executeMethod(method);
			} catch (IllegalArgumentException e) {
				throw new VersionServiceException("Connection error while retriving the latest plugin version", e);
			}
			InputStream is = method.getResponseBodyAsStream();
			SAXBuilder builder = new SAXBuilder();
			builder.setValidation(false);
			Document doc = builder.build(is);
			return new VersionInfo(doc, VersionInfo.Type.STABLE);
		} catch (IOException e) {
			throw new VersionServiceException("Connection error while retriving the latest plugin version", e);
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

		/**
		 * Only for internal use (package scope)
		 * @param doc doc
		 * @param type type
		 */
		VersionInfo(Document doc, VersionInfo.Type type) throws VersionServiceException, IncorrectVersionException {
			switch (type) {
				case STABLE:
					version = new Version(getValue("/response/versions/stable/latestVersion", doc));
					downloadUrl = getValue("/response/versions/stable/downloadUrl", doc);
					break;
				case UNSTABLE:
					version = new Version(getValue("/response/versions/unstable/latestVersion", doc));
					downloadUrl = getValue("/response/versions/unstable/downloadUrl", doc);
					break;
				default:
					throw new VersionServiceException("neither stable nor unstable");
			}
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
					throw new VersionServiceException("Error while parsing " + PluginUtil.VERSION_INFO_URL);
				}
			} catch (JDOMException e) {
				throw new VersionServiceException("Error while parsing " + PluginUtil.VERSION_INFO_URL, e);
			}
			return element.getValue();
		}

		public Version getVersion() {
			return version;
		}

		public String getDownloadUrl() {
			return downloadUrl;
		}
	}
}
