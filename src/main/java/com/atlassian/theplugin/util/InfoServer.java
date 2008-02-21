package com.atlassian.theplugin.util;

import com.atlassian.theplugin.exception.VersionServiceException;
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
	// todo lguminski/jjaroczynski change to real server's instance
	public static final String INFO_SERVER_URL = "http://xxx.com";

	public InfoServer(String serviceUrl, long uid) {
		this.serviceUrl = serviceUrl;
		this.uid = uid;
	}

	public String getLatestPluginVersion() throws VersionServiceException {
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
			return getVersion(doc);
		} catch (IOException e) {
			throw new VersionServiceException("Connection error while retriving the latest plugin version", e);
		} catch (JDOMException e) {
			throw new VersionServiceException("Error while parsing xml response from version service server", e);
		}
	}

	private String getVersion(Document doc) throws JDOMException {
		XPath xpath = XPath.newInstance("/response/latestVersion");
		Element element = (Element) xpath.selectSingleNode(doc);
		return element.getValue();
	}
}
