package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.util.InfoServer;
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
	public static final String VERSION = "0.2.0";
	private long uid;
	private String mockBaseUrl;

	@Override
	protected void setUp() throws Exception {
		config = new PluginConfigurationBean();
		uid = config.getUid();

		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockServer = new JettyMockServer(httpServer);
	}

	public void testGetLatestVersion(){
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback());
		InfoServer infoServer = new InfoServer(mockBaseUrl + GET_LATEST_VERSION_URL, uid);

		String version = null;
		try {
			version = infoServer.getLatestPluginVersion();
		} catch (VersionServiceException e) {
			fail(e.getMessage());
		}
		assertNotNull(version);
		assertEquals(VERSION, version);
	}


	private class PingCallback implements JettyMockServer.Callback {
		private static final String RESOURCE_BASE =	"/mock/utils/";

		public PingCallback() {
			super();	//To change body of overridden methods use File | Settings | File Templates.
		}

		public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			sb.append("<response><latestVersion>");
			sb.append(NewVersionCheckerTest.this.VERSION);
			sb.append("</latestVersion></response>");
			try {
				outputStream.write(sb.toString().getBytes("UTF-8"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
