package com.atlassian.theplugin.util;

import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.net.URLConnection;
import java.io.InputStream;
import java.io.IOException;

import com.atlassian.theplugin.exception.VersionServiceException;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Feb 19, 2008
 * Time: 11:15:21 AM
 * To change this template use File | Settings | File Templates.
 */
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
		URLConnection c = null;
		try {
			c = HttpConnectionFactory.getConnection(serviceUrl + "?uid=" + uid);
			InputStream is = c.getInputStream();
			SAXBuilder builder = new SAXBuilder();
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
