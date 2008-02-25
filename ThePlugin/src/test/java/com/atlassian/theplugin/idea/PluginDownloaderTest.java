package com.atlassian.theplugin.idea;

import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	private static final String SOME_VERSION = "0.3.0";


	protected void setUp() throws Exception {
		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockServer = new JettyMockServer(httpServer);
		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort() + DOWNLOAD_PATH;
		downloader = new PluginDownloader(SOME_VERSION, mockBaseUrl);
	}

	public void testDownloadPluginFromServer() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		mockServer.expect(DOWNLOAD_BASE, new ArchiveRepositoryProviderCallback());

		Class[] paramTypes = {String.class};
		Method method = PluginDownloader.class.getDeclaredMethod("downloadPluginFromServer", paramTypes);
		method.setAccessible(true);
		File localFile = null;
		try {
			localFile = (File) method.invoke(downloader, new Object[]{SOME_VERSION});

		}
		catch (InvocationTargetException ex) {
			fail("Invocation of the downloadPluginFromServer method failed: " + ex.getMessage());
		}
		finally {
			if (localFile != null) {
				localFile.delete();
			}
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
