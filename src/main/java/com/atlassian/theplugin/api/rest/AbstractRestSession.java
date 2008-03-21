package com.atlassian.theplugin.api.rest;

import com.atlassian.theplugin.util.HttpClientFactory;
import com.atlassian.theplugin.util.UrlUtil;
import com.atlassian.theplugin.api.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.api.RemoteApiSessionExpiredException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Communication stub for REST API.
 */
public abstract class AbstractRestSession {
	protected final String baseUrl;
	protected String userName;
	protected String password;
	protected HttpClient client = null;

	private final Object clientLock = new Object();

	/**
	 * Public constructor for AbstractRestSession
	 *
	 * @param baseUrl base URL for server instance
	 */
	public AbstractRestSession(String baseUrl) throws RemoteApiMalformedUrlException {
		if (baseUrl == null) {
			throw new RemoteApiMalformedUrlException("Malformed server URL: null");
		}
		this.baseUrl = UrlUtil.removeUrlTrailingSlashes(baseUrl);
		try {
			new URL(baseUrl);
			validateUrl(baseUrl);
		} catch (MalformedURLException e) {
			throw new RemoteApiMalformedUrlException("Malformed server URL: " + baseUrl, e);
		}
	}

	private void validateUrl(String urlString) throws MalformedURLException {
		// validate URL first
		try {
			URL url = new URL(urlString);
			// check the host name
			if (url.getHost().length() == 0) {
				throw new MalformedURLException("Url must contain valid host.");
			}
			// check the port number
			if (url.getPort() >= 2 * Short.MAX_VALUE) {
				throw new MalformedURLException("Url port invalid");
			}
		} catch (MalformedURLException e) {
			throw new MalformedURLException("Url must contain valid host.");
		}
	}

	protected Document retrieveGetResponse(String urlString) throws IOException, JDOMException, RemoteApiSessionExpiredException {
		validateUrl(urlString);

		Document doc = null;
		synchronized (clientLock) {
			if (client == null) {
				client = HttpClientFactory.getClient();
			}

			GetMethod method = new GetMethod(urlString);

			try {
				method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
				adjustHttpHeader(method);

				client.executeMethod(method);

				if (method.getStatusCode() != HttpStatus.SC_OK) {
					throw new IOException(
							"HTTP " + method.getStatusCode() + " (" + HttpStatus.getStatusText(method.getStatusCode())
									+ ")\n" + method.getStatusText());
				}

				SAXBuilder builder = new SAXBuilder();
				doc = builder.build(method.getResponseBodyAsStream());

				preprocessResult(doc);
			} catch (NullPointerException e) {
				throw (IOException) new IOException("Connection error").initCause(e);
			} finally {
				method.releaseConnection();
			}
		}

		return doc;
	}

	protected Document retrievePostResponse(String urlString, Document request)
			throws IOException, JDOMException, RemoteApiSessionExpiredException {
		validateUrl(urlString);


		Document doc = null;
		synchronized (clientLock) {
			if (client == null) {
				client = HttpClientFactory.getClient();
			}

			PostMethod method = new PostMethod(urlString);

			try {
				method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
				adjustHttpHeader(method);

				XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
				method.setRequestEntity(
						new StringRequestEntity(serializer.outputString(request), "application/xml", "UTF-8"));

				client.executeMethod(method);

				if (method.getStatusCode() != HttpStatus.SC_OK) {
					throw new IOException("HTTP status code " + method.getStatusCode() + ": " + method.getStatusText());
				}

				SAXBuilder builder = new SAXBuilder();
				doc = builder.build(method.getResponseBodyAsStream());

				preprocessResult(doc);
			} catch (NullPointerException e) {
				throw (IOException) new IOException("Connection error").initCause(e);
			} finally {
				method.releaseConnection();
			}
		}
		return doc;
	}

	protected abstract void adjustHttpHeader(HttpMethod method);

	protected abstract void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException;
}