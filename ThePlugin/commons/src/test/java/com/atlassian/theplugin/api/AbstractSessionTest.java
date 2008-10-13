package com.atlassian.theplugin.api;

import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.remoteapi.ErrorResponse;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.mortbay.jetty.Server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

/**
 * User: pmaruszak
 */
public abstract class AbstractSessionTest extends TestCase {
	protected static final String USER_NAME = "someUser";
	protected static final String PASSWORD = "somePassword";

	private Server server;
	protected JettyMockServer mockServer;
	protected String mockBaseUrl;

	@Override
	protected void setUp() throws Exception {
        ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

        server = new Server(0);
		server.start();		

		mockBaseUrl = "http://localhost:" + server.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(server);
    }

	@Override
	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		server.stop();
	}


	private void tryMalformedUrl(final String url) {
		try {
			ProductSession apiHandler = getProductSession(url);
			apiHandler.login(USER_NAME, PASSWORD.toCharArray());
			fail("Exception expected but not thrown");
		} catch (RemoteApiException exception) {

			assertNotNull("Exception expected", exception);
			assertTrue("MalformedURLExceptionException expected",
					exception.getCause() instanceof MalformedURLException);
			assertEquals("Malformed server URL: " + url, exception.getMessage());
		}

	}

	protected abstract ProductSession getProductSession(final String url) throws RemoteApiMalformedUrlException;

	public void testMalformedUrlLogin() {
		tryMalformedUrl("noprotocol.url/path");
		tryMalformedUrl("http:localhost/path");
		tryMalformedUrl("http:/localhost/path");
		tryMalformedUrl("http:///localhost/path");
		tryMalformedUrl("http:localhost");
		tryMalformedUrl("http:/localhost");
		tryMalformedUrl("http:///localhost");
		tryMalformedUrl("http://");
		tryMalformedUrl("ncxvx:/localhost/path");
		tryMalformedUrl("ncxvx:///localhost/path");
		tryMalformedUrl("ncxvx://localhost/path");
		tryMalformedUrl("ncxvx:///localhost/path");
		tryMalformedUrl("https:localhost/path");
		tryMalformedUrl("https:/localhost/path");
		tryMalformedUrl("https:///localhost/path");
		tryMalformedUrl("https:localhost");
		tryMalformedUrl("https:/localhost");
		tryMalformedUrl("https:///localhost");
		tryMalformedUrl("https://");
		tryMalformedUrl("http::localhost/path");
		tryMalformedUrl("http://loca:lhost/path");
	}

	public void testWrongUrlLogin() throws Exception {
		ErrorResponse error = new ErrorResponse(400, "Bad Request");
		mockServer.expect("/wrongurl" + getLoginUrl(), error);
		RemoteApiLoginException exception = null;

		try {
			ProductSession apiHandler = getProductSession(mockBaseUrl + "/wrongurl");
			apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		} catch (RemoteApiLoginException ex) {
			exception = ex;
		}
		mockServer.verify();

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertSame(IOException.class, exception.getCause().getClass());
		assertTrue(exception.getMessage().contains(error.getErrorMessage()));
	}

	public void testNonExistingServerLogin() throws Exception {
		RemoteApiLoginException exception = null;

		try {
			ProductSession apiHandler = getProductSession("http://non.existing.server.utest");
			apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		} catch (RemoteApiLoginException ex) {
			exception = ex;
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertSame("UnknownHostException expected", UnknownHostException.class, exception.getCause().getClass());
		assertEquals("Checking exception message", "Unknown host: non.existing.server.utest", exception.getMessage());
	}


	public void testWrongUserLogin() throws Exception {
		mockServer.expect(getLoginUrl(), getLoginCallback(true));

		try {
			ProductSession apiHandler = getProductSession(mockBaseUrl);
			apiHandler.login(USER_NAME, PASSWORD.toCharArray()); // mock will fail this
			fail();
		} catch (RemoteApiLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}

		mockServer.verify();
	}

	protected abstract JettyMockServer.Callback getLoginCallback(boolean isFail);
	protected abstract String getLoginUrl();

}
