package com.atlassian.theplugin.util;

import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.idea.PluginInfoUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.InputStream;

public class InfoServer {
	private String serviceUrl;
	private long uid;

	public InfoServer(String serviceUrl, long uid) {
		this.serviceUrl = serviceUrl;
		this.uid = uid;
	}

	public VersionInfo getLatestPluginVersion() throws VersionServiceException {
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
			return new VersionInfo(doc);
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
		private Document doc;
		private String version;
		private String downloadUrl;

		public VersionInfo(Document doc) {
			this.doc = doc;
		}

		public VersionInfo(String version, String downloadUrl) {
			this.version = version;
			this.downloadUrl = downloadUrl;
		}

		public String getVersion() throws VersionServiceException {
			if (version == null) {
				version = getValue("/response/latestStableVersion");
			}
			return version;
		}

		private String getValue(String path) throws VersionServiceException {
			XPath xpath;
			Element element;
			try {
				xpath = XPath.newInstance(path);
				element = (Element) xpath.selectSingleNode(doc);
			} catch (JDOMException e) {
				throw new VersionServiceException("Error while parsing " + PluginInfoUtil.VERSION_INFO_URL, e);
			}
			return element.getValue();
		}

		public String getDownloadUrl() throws VersionServiceException {
			if (downloadUrl == null) {
				downloadUrl = getValue("/response/downloadUrl");
			}
			return downloadUrl;
		}
	}
}
