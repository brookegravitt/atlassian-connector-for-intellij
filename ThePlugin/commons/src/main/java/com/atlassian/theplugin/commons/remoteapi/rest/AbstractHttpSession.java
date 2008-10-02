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

package com.atlassian.theplugin.commons.remoteapi.rest;

import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.util.HttpClientFactory;
import com.atlassian.theplugin.commons.util.UrlUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

/**
 * Communication stub for lightweight XML based APIs.
 */
public abstract class AbstractHttpSession {
    protected final String baseUrl;
    protected String userName;
    protected String password;
    protected HttpClient client = null;

    private final Object clientLock = new Object();

    private static ThreadLocal<URL> url = new ThreadLocal<URL>();

    // TODO: replace this with a proper cache to ensure automatic purging.
    private final Map<String, CacheRecord> cache =
        new HashMap<String, CacheRecord>();

    /**
     * This class holds an HTTP response body, together with its last
     * modification time and Etag.
     */
    private final class CacheRecord {
        private final byte[] document;
        private final String lastModified;
        private final String etag;

        private CacheRecord(byte[] document, String lastModified, String etag) {
            if (document == null || lastModified == null || etag == null) {
                throw new IllegalArgumentException("null");
            } else {
                this.document = document;
                this.lastModified = lastModified;
                this.etag = etag;
            }
        }

        public byte[] getDocument() {
            return document;
        }

        public String getLastModified() {
            return lastModified;
        }

        public String getEtag() {
            return etag;
        }
    }

    public static URL getUrl() {
        return url.get();
    }

	public static void setUrl(final URL urlString) {
		url.set(urlString);
	}

	public static void setUrl(final String urlString) throws MalformedURLException {
		setUrl(new URL(urlString));
	}

	/**
     * Public constructor for AbstractHttpSession
     *
     * @param baseUrl base URL for server instance
     * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException
     *          for malformed url
     */
    public AbstractHttpSession(String baseUrl) throws RemoteApiMalformedUrlException {

        this.baseUrl = UrlUtil.removeUrlTrailingSlashes(baseUrl);

        try {
            UrlUtil.validateUrl(baseUrl);
        } catch (MalformedURLException e) {
            throw new RemoteApiMalformedUrlException("Malformed server URL: " + baseUrl, e);
        }
    }

    protected Document retrieveGetResponse(String urlString)
            throws IOException, JDOMException, RemoteApiSessionExpiredException {
        return retrieveGetResponse(urlString, true);
    }

    protected Document retrieveGetResponse(String urlString, boolean expectResponse)
            throws IOException, JDOMException, RemoteApiSessionExpiredException {

        byte[] result = doConditionalGet(urlString);
        Document doc = null;
        if (expectResponse) {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(new ByteArrayInputStream(result));
            preprocessResult(doc);
        }
        return doc;
    }

    protected byte[] doConditionalGet(String urlString) throws IOException, JDOMException, RemoteApiSessionExpiredException {

        UrlUtil.validateUrl(urlString);
		setUrl(urlString);
		synchronized (clientLock) {
            if (client == null) {
                try {
                    client = HttpClientFactory.getClient();
                } catch (HttpProxySettingsException e) {
                    throw (IOException) new IOException("Connection error. Please set up HTTP Proxy settings").initCause(e);
                }
            }

            GetMethod method = new GetMethod(urlString);

            CacheRecord cacheRecord = cache.get(urlString);
            if (cacheRecord != null) {
                System.out.println(String.format("%s in cache, adding If-Modified-Since: %s and If-None-Match: %s headers.",
                    urlString, cacheRecord.getLastModified(), cacheRecord.getEtag()));
                method.addRequestHeader("If-Modified-Since", cacheRecord.getLastModified());
                method.addRequestHeader("If-None-Match", cacheRecord.getEtag());
            }
            try {
                method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
                method.getParams().setSoTimeout(client.getParams().getSoTimeout());
                adjustHttpHeader(method);

                client.executeMethod(method);

                if (method.getStatusCode() == HttpStatus.SC_NOT_MODIFIED && cacheRecord != null) {
                    System.out.println("Cache record valid, using cached value: " + new String(cacheRecord.getDocument()));
                    return cacheRecord.getDocument().clone();
                } else if (method.getStatusCode() != HttpStatus.SC_OK) {
                    throw new IOException(
                            "HTTP " + method.getStatusCode() + " (" + HttpStatus.getStatusText(method.getStatusCode())
                                    + ")\n" + method.getStatusText());
                } else {
                    System.out.println("Received GET response document.");
                    final byte[] result = method.getResponseBody();
                    final String lastModified = method.getResponseHeader("Last-Modified") == null ? null
							: method.getResponseHeader("Last-Modified").getValue();
                    final String eTag = method.getResponseHeader("Etag") == null ? null 
							: method.getResponseHeader("Etag").getValue();

                    if (lastModified != null && eTag != null) {
                        cacheRecord = new CacheRecord(result, lastModified, eTag);
                        cache.put(urlString, cacheRecord);
                        System.out.println("Latest GET response document placed in cache: " + new String(result));
                    }
                    return result.clone();
                }
            } catch (NullPointerException e) {
                throw (IOException) new IOException("Connection error").initCause(e);
            } finally {
                method.releaseConnection();
            }
        }
    }

    protected byte[] retrieveGetResponseAsBytes(String urlString)
            throws IOException, JDOMException, RemoteApiSessionExpiredException {
        UrlUtil.validateUrl(urlString);
		setUrl(urlString);
		synchronized (clientLock) {
            if (client == null) {
                try {
                    client = HttpClientFactory.getClient();
                } catch (HttpProxySettingsException e) {
                    throw (IOException) new IOException("Connection error. Please set up HTTP Proxy settings").initCause(e);
                }
            }

            GetMethod method = new GetMethod(urlString);

            try {
                method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
                method.getParams().setSoTimeout(client.getParams().getSoTimeout());
                adjustHttpHeader(method);

                client.executeMethod(method);

                if (method.getStatusCode() != HttpStatus.SC_OK) {
                    throw new IOException(
                            "HTTP " + method.getStatusCode() + " (" + HttpStatus.getStatusText(method.getStatusCode())
                                    + ")\n" + method.getStatusText());
                }

                return method.getResponseBody();
            } catch (NullPointerException e) {
                throw (IOException) new IOException("Connection error").initCause(e);
            } finally {
                method.releaseConnection();
            }
        }
    }

    protected Document retrievePostResponse(String urlString, Document request)
            throws IOException, JDOMException, RemoteApiSessionExpiredException {
        return retrievePostResponse(urlString, request, true);
    }

    protected Document retrievePostResponse(String urlString, Document request, boolean expectResponse)
            throws IOException, JDOMException, RemoteApiSessionExpiredException {
        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String requestString = serializer.outputString(request);
        return retrievePostResponse(urlString, requestString, expectResponse);
    }

    protected Document retrievePostResponse(String urlString, String request, boolean expectResponse)
            throws IOException, JDOMException, RemoteApiSessionExpiredException {
        UrlUtil.validateUrl(urlString);
		setUrl(urlString);
		Document doc = null;
        synchronized (clientLock) {
            if (client == null) {
                try {
                    client = HttpClientFactory.getClient();
                } catch (HttpProxySettingsException e) {
                    throw (IOException) new IOException("Connection error. Please set up HTTP Proxy settings").initCause(e);
                }
            }

            PostMethod method = new PostMethod(urlString);

            try {
                method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
                method.getParams().setSoTimeout(client.getParams().getSoTimeout());
                adjustHttpHeader(method);

                if (request != null && !"".equals(request)) {
                    method.setRequestEntity(
                            new StringRequestEntity(request, "application/xml", "UTF-8"));
                }

                client.executeMethod(method);

                if (method.getStatusCode() != HttpStatus.SC_OK) {
                    throw new IOException("HTTP status code " + method.getStatusCode() + ": " + method.getStatusText());
                }

                if (expectResponse) {
                    SAXBuilder builder = new SAXBuilder();
                    doc = builder.build(method.getResponseBodyAsStream());
                    preprocessResult(doc);
                }
            } catch (NullPointerException e) {
                throw (IOException) new IOException("Connection error").initCause(e);
            } finally {
                method.releaseConnection();
            }
        }
        return doc;
    }


	protected Document retrieveDeleteResponse(String urlString, boolean expectResponse)
            throws IOException, JDOMException, RemoteApiSessionExpiredException {
        UrlUtil.validateUrl(urlString);

        Document doc = null;
        synchronized (clientLock) {
            if (client == null) {
                try {
                    client = HttpClientFactory.getClient();
                } catch (HttpProxySettingsException e) {
                    throw (IOException) new IOException("Connection error. Please set up HTTP Proxy settings").initCause(e);
                }
            }

            DeleteMethod method = new DeleteMethod(urlString);

            try {
                method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
                method.getParams().setSoTimeout(client.getParams().getSoTimeout());
                adjustHttpHeader(method);

                client.executeMethod(method);

                if (method.getStatusCode() != HttpStatus.SC_OK) {
                    throw new IOException("HTTP status code " + method.getStatusCode() + ": " + method.getStatusText());
                }

                if (expectResponse) {
                    SAXBuilder builder = new SAXBuilder();
                    doc = builder.build(method.getResponseBodyAsStream());
                    preprocessResult(doc);
                }
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

    public static String getServerNameFromUrl(String urlString) {
        int pos = urlString.indexOf("://");
        if (pos != -1) {
            urlString = urlString.substring(pos + 1 + 2);
        }
        pos = urlString.indexOf("/");
        if (pos != -1) {
            urlString = urlString.substring(0, pos);
        }
        return urlString;
    }
}
