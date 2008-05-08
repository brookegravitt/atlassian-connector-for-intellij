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

import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.util.HttpClientFactory;
import com.atlassian.theplugin.commons.configuration.*;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpMethod;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.mortbay.jetty.Server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Apr 23, 2008
 * Time: 3:32:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class AbstractHttpSessionTest extends TestCase {
	private Server httpServer;
	private JettyMockServer mockServer;
	private static final String SOME_URL = "/some_url";


	@Override
	protected void setUp() throws Exception {
		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

        ConfigurationFactory.setConfiguration(new PluginConfigurationBean());
        mockServer = new JettyMockServer(httpServer);
	}

    public void testRetrieveGetResponseWithDataTransferTimeout() throws RemoteApiMalformedUrlException, IOException, RemoteApiSessionExpiredException, JDOMException {
		int timeout; // 7 sec
		long t1;
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort() + SOME_URL;
		mockServer.expect(SOME_URL, new TimeoutingOnDataTransferCallback());
		TestHttpSession session = new TestHttpSession(mockBaseUrl);

		timeout = 100;
		HttpClientFactory.setDataTimeout(timeout);
		t1 = System.currentTimeMillis();
		try {
			session.retrieveGetResponse(mockBaseUrl);
			fail("It should fail but it didn't1");
		} catch (SocketTimeoutException e) {
			long diff = System.currentTimeMillis() - t1;
			if (diff > timeout * 20) { //too slow
				fail("Timeout doesn't work.");
			}
		}
	}

	private class TimeoutingOnDataTransferCallback implements JettyMockServer.Callback {
		public void onExpectedRequest(String s, HttpServletRequest request, HttpServletResponse response) throws Exception {
			response.setContentType("text/xml");
			response.getOutputStream().write("<a/>".getBytes("UTF-8"));
			response.getOutputStream().flush();
			Thread.sleep(10000);
		}
	}

	private class TestHttpSession extends AbstractHttpSession {

		@Override
		protected Document retrieveGetResponse(String urlString) throws IOException, JDOMException, RemoteApiSessionExpiredException {
			return super.retrieveGetResponse(urlString);	//To change body of overridden methods use File | Settings | File Templates.
		}

		@Override
		protected Document retrievePostResponse(String urlString, Document request) throws IOException, JDOMException, RemoteApiSessionExpiredException {
			return super.retrievePostResponse(urlString, request);	//To change body of overridden methods use File | Settings | File Templates.
		}

		/**
		 * Public constructor for AbstractHttpSession
		 *
		 * @param baseUrl base URL for server instance
		 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException
		 *          for malformed url
		 */
		public TestHttpSession(String baseUrl) throws RemoteApiMalformedUrlException {
			super(baseUrl);
		}

		protected void adjustHttpHeader(HttpMethod method) {

		}

		protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {

		}

	}
}
