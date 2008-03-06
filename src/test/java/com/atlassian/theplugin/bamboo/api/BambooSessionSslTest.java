package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.api.bamboomock.LoginCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.LogoutCallback;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.SslSocketConnector;

public class BambooSessionSslTest extends TestCase {
	private static final String USER_NAME = "someSslUser";
	private static final String PASSWORD = "SomeSslPass";


	private Server server;
	private JettyMockServer mockServer;
	private String mockBaseUrl;

	protected void setUp() throws Exception {
		String keystoreLocation = getClass().getResource("/mock/selfSigned.keystore").toExternalForm();

		SslSocketConnector sslConnector = new SslSocketConnector();

		sslConnector.setPort(0);
		sslConnector.setKeystore(keystoreLocation);
		sslConnector.setPassword("password");
		sslConnector.setKeyPassword("password");

		server = new Server();

		server.addConnector(sslConnector);
		server.start();

		mockBaseUrl = "https://localhost:" + sslConnector.getLocalPort();

		mockServer = new JettyMockServer(server);
	}

	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		server.stop();
	}

	public void testSuccessBambooLoginOnSSL() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback(LoginCallback.AUTH_TOKEN));

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

}
