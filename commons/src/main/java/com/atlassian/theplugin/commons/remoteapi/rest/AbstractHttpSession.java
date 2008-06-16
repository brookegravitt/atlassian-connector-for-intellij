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
import java.net.MalformedURLException;

/**
 * Communication stub for lightweight XML based APIs.
 */
public abstract class AbstractHttpSession {
    protected final String baseUrl;
    protected String userName;
    protected String password;
    protected HttpClient client = null;

    private final Object clientLock = new Object();

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
}