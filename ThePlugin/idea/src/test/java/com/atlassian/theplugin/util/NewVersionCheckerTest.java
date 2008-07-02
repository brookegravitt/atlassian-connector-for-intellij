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

import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.exception.IncorrectVersionException;
import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.commons.util.Version;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Feb 19, 2008
 * Time: 10:06:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class NewVersionCheckerTest extends TestCase {
	private org.mortbay.jetty.Server httpServer;
	private JettyMockServer mockServer;
	private PluginConfigurationBean config;
	private static final String GET_LATEST_VERSION_URL = "/GetLatestVersion";
	public static final String VERSION = "0.2.0, SVN:11";
	private long uid;

	@Override
	protected void setUp() throws Exception {
		config = new PluginConfigurationBean();
		uid = config.getGeneralConfigurationData().getUid();

		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockServer = new JettyMockServer(httpServer);
	}

	public void testFake(){
		assertTrue(true);
	}
	public void xtestGetLatestVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback());
		InfoServer.VersionInfo versionInfo = null;
		try {
			versionInfo = InfoServer.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, uid, false);
		} catch (VersionServiceException e) {
			fail(e.getMessage());
		}
		assertNotNull(versionInfo);
		assertEquals(new Version(VERSION), versionInfo.getVersion());
	}


	public void fakeTest(){
		assertTrue(true);
	}
	private class PingCallback implements JettyMockServer.Callback {

		public PingCallback() {
			super();	//To change body of overridden methods use File | Settings | File Templates.
		}
		
		public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response)
				throws Exception {
			
			response.setContentType("text/xml");
			assertTrue(request.getPathInfo().endsWith(GET_LATEST_VERSION_URL));

			final String[] uids = request.getParameterValues("uid");

			assertEquals(1, uids.length);

			final String uid = uids[0];

			assertEquals(uid, String.valueOf(NewVersionCheckerTest.this.uid));

			createResponse(response.getOutputStream());
			response.getOutputStream().flush();
		}

		private void createResponse(ServletOutputStream outputStream) {
			StringBuffer sb = new StringBuffer();
			sb.append("<response><version><number>");
			sb.append(NewVersionCheckerTest.VERSION);
			sb.append("</number>");
			sb.append("<downloadUrl>");
			sb.append("http://somedomain.com");
			sb.append("</downloadUrl>");
			sb.append("</version></response>");
			try {
				outputStream.write(sb.toString().getBytes("UTF-8"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
