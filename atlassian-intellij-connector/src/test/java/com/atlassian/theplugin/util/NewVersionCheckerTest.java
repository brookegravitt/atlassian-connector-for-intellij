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

import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.exception.IncorrectVersionException;
import com.atlassian.theplugin.commons.util.Version;
import com.atlassian.theplugin.configuration.IdeaPluginConfigurationBean;
import com.atlassian.theplugin.exception.VersionServiceException;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author lguminski
 */
public class NewVersionCheckerTest extends TestCase {
	private org.mortbay.jetty.Server httpServer;
	private JettyMockServer mockServer;
	private static final String GET_LATEST_VERSION_URL = "/GetLatestVersion";
	public static final String VERSION = "0.2.0, SVN:18";
	public static final String VERSION_ALPHA = "0.2.0-alpHa, SVN:12";
	public static final String VERSION_ALPHA1 = "0.2.0-alpHa-1, SVN:13";
	public static final String VERSION_ALPHA123456 = "0.2.0-alpHa-123456, SVN:14";
	public static final String VERSION_BETA = "0.2.0-BETa, SVN:15";
	public static final String VERSION_BETA1 = "0.2.0-bEta-1, SVN:16";
	public static final String VERSION_BETA123456 = "0.2.0-Beta-123456, SVN:18";
	public static final String VERSION_SNAPSHOT_LT = "0.2.0-SnaPSHot, SVN:10";
	public static final String VERSION_SNAPSHOT_GT = "0.2.0-SnaPSHot, SVN:17";

	private long uid;
	private UsageStatisticsGenerator usageStatisticsGenerator;


	@Override
	protected void setUp() throws Exception {
		final IdeaPluginConfigurationBean config = new IdeaPluginConfigurationBean();
		uid = config.getGeneralConfigurationData().getUid();
		ConfigurationFactory.setConfiguration(config);

		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockServer = new JettyMockServer(httpServer);

		usageStatisticsGenerator = new UsageStatisticsGenerator() {
			public String getStatisticsUrlSuffix() {
				return "uid=" + uid;
			}
		};

	}

	public void testGetLatestVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		assertNotNull(versionInfo);
		Version newVersion = new Version(VERSION);

		assertFalse(newVersion.greater(versionInfo.getVersion()));
		assertFalse(versionInfo.getVersion().greater(newVersion));
	}

	public void testGetLatestAlphaVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_ALPHA));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION);

		assertFalse(versionInfo.getVersion().greater(newVersion));
	}

	public void testGetLatestBetaVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_BETA));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION);

		assertFalse(versionInfo.getVersion().greater(newVersion));
	}

	public void testGetLatestSnapshotVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_SNAPSHOT_LT));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION);

		assertFalse(versionInfo.getVersion().greater(newVersion));

	}

	public void testGetLatestBetaCompareToAlfaVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_BETA));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION_ALPHA);

		assertTrue(versionInfo.getVersion().greater(newVersion));
	}

	public void testGetLatestBetaCompareToSnapshotLtVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_BETA));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION_SNAPSHOT_LT);

		assertTrue(versionInfo.getVersion().greater(newVersion));

	}

	public void testGetLatestBetaCompareToSnapshotGtVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_BETA));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION_SNAPSHOT_GT);

		assertFalse(versionInfo.getVersion().greater(newVersion));

	}

	public void testGetLatestAlphaCompareToSnapshotLtVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_ALPHA));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION_SNAPSHOT_LT);

		assertTrue(versionInfo.getVersion().greater(newVersion));

	}

	public void testGetLatestAlphaCompareToSnapshotGtVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_ALPHA));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION_SNAPSHOT_GT);

		assertFalse(versionInfo.getVersion().greater(newVersion));

	}

	public void testGetLatestAlphaToAlphaVersion() throws VersionServiceException, IncorrectVersionException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_ALPHA1));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION_ALPHA);

		assertTrue(versionInfo.getVersion().greater(newVersion));

	}


	public void testGetWithReleaseNotes() throws IncorrectVersionException, VersionServiceException {
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();
		final String notesUrl = "http://myurl";
		final String myReleaseNotes = "my <%&$*(> string\nfdaljsf";
		mockServer.expect(GET_LATEST_VERSION_URL, new PingCallback(NewVersionCheckerTest.VERSION_ALPHA1, "<![CDATA[" +
				myReleaseNotes + "]]>", notesUrl));
		InfoServer.VersionInfo versionInfo = InfoServer
				.getLatestPluginVersion(mockBaseUrl + GET_LATEST_VERSION_URL, usageStatisticsGenerator);
		Version newVersion = new Version(VERSION_ALPHA);

		assertTrue(versionInfo.getVersion().greater(newVersion));
		assertEquals(notesUrl, versionInfo.getReleaseNotesUrl());
		assertEquals(myReleaseNotes, versionInfo.getReleaseNotes());
	}


	private class PingCallback implements JettyMockServer.Callback {
		private final String version;
		private final String releaseNotes;
		private final String releaseNotesUrl;

		public PingCallback(String version) {
			this(version, null, null);
		}

		public PingCallback(final String version, final String releaseNotes, final String releaseNotesUrl) {
			this.version = version;
			this.releaseNotes = releaseNotes;
			this.releaseNotesUrl = releaseNotesUrl;
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
			StringBuilder sb = new StringBuilder();
			sb.append("<response><version><number>");
			sb.append(version);
			sb.append("</number>");
			sb.append("<downloadUrl>");
			sb.append("http://somedomain.com");
			sb.append("</downloadUrl>");
			sb.append("<releaseNotes>");
			sb.append(releaseNotes != null ? releaseNotes : "N/A");
			sb.append("</releaseNotes>");
			sb.append("<releaseNotesUrl>");
			sb.append(releaseNotesUrl != null ? releaseNotesUrl : "N/A");
			sb.append("</releaseNotesUrl>");
			sb.append("</version></response>");
			try {
				outputStream.write(sb.toString().getBytes("UTF-8"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}
}
