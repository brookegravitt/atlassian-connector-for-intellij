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

package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.util.Version;
import com.atlassian.theplugin.configuration.IdeaPluginConfigurationBean;
import com.atlassian.theplugin.util.InfoServer;
import com.intellij.openapi.util.io.FileUtil;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-20
 * Time: 12:39:16
 * To change this template use File | Settings | File Templates.
 */
public class PluginDownloaderTest extends TestCase {
	private PluginDownloader downloader;
	private org.mortbay.jetty.Server httpServer;
	private JettyMockServer mockServer;
	private static final String DOWNLOAD_BASE = "/GetPackage";
	private static final String DOWNLOAD_PATH = DOWNLOAD_BASE + "?pack=" +
			PluginDownloader.PLUGIN_ID_TOKEN +
			"&version=" +
			PluginDownloader.VERSION_TOKEN +
			"&fileType=.zip";
	private static final String SOME_VERSION = "0.3.0, SVN:2233";
	private PluginConfiguration pluginConfiguration = new IdeaPluginConfigurationBean();

	@Override
	protected void setUp() throws Exception {
		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockServer = new JettyMockServer(httpServer);
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort() + DOWNLOAD_PATH;
		InfoServer.VersionInfo newVersion = new InfoServer.VersionInfo(new Version(SOME_VERSION), mockBaseUrl);
		downloader = new PluginDownloader(newVersion, pluginConfiguration.getGeneralConfigurationData());
	}

	@Override
	protected void tearDown() throws Exception {
		httpServer.stop();
	}

	public void testDownloadPluginFromServer() throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, NoSuchFieldException, IOException {
		mockServer.expect(DOWNLOAD_BASE, new ArchiveRepositoryProviderCallback());

		File localFile = null;
		try {
			localFile = downloader.downloadPluginFromServer(SOME_VERSION, new File(FileUtil.getTempDirectory()));
		} finally {
			if (localFile != null) {
				localFile.delete();
			}
		}
	}

	public void testTimeoutedDownloadPluginFromServer() throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, NoSuchFieldException, IOException {
		mockServer.expect(DOWNLOAD_BASE, new TimeoutProviderCallback());

		File localFile = null;
		try {
			downloader.setReadTimeout(10);
			downloader.setTimeout(10);
			localFile = downloader.downloadPluginFromServer(SOME_VERSION, new File(FileUtil.getTempDirectory()));
			fail("Invocation of the downloadPluginFromServer method failed: timeout doesn't work");
		} catch (SocketTimeoutException ex) {
			// ok
		} finally {
			if (localFile != null) {
				localFile.delete();
			}
		}
	}

	private class TimeoutProviderCallback implements JettyMockServer.Callback {

		public void onExpectedRequest(String s, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
			Thread.sleep(2000);
		}
	}

	private class ArchiveRepositoryProviderCallback implements JettyMockServer.Callback {
		private static final String FILE_CONTENT = "alalalalalalal";

		public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
			response.setContentType("application/zip");
			//response.setContentType("text/xml");
			assertTrue(request.getPathInfo().endsWith(DOWNLOAD_BASE));

			final String pack = request.getParameterValues("pack")[0];
			final String version = request.getParameterValues("version")[0];

			assertEquals(version, SOME_VERSION);

			createResponse(response.getOutputStream());
			response.getOutputStream().flush();
		}


		/**
		 * Generating sample zip file and write it to the outputStream
		 *
		 * @param outputStream
		 * @throws IOException
		 */
		private void createResponse(ServletOutputStream outputStream) throws IOException {
			ZipOutputStream zos = new ZipOutputStream(outputStream);
			ZipEntry zipentry = new ZipEntry("sample content");
			byte[] myBytes = FILE_CONTENT.getBytes();
			zos.putNextEntry(zipentry);
			zos.write(myBytes, 0, myBytes.length);
			zos.closeEntry();
			zos.finish();
			zos.close();
		}
	}
}
