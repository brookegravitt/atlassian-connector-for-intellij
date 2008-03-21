package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.LoginCallback;
import com.atlassian.theplugin.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.remoteapi.RemoteApiMalformedUrlException;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;

import java.util.ArrayList;
import java.util.Collection;

public class CrucibleServerFacadeConnectionTest extends TestCase {
	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";

	private org.mortbay.jetty.Server httpServer;
	private JettyMockServer mockServer;
	private String mockBaseUrl;
	public static final String INVALID_PROJECT_KEY = "INVALID project key";
	private CrucibleServerFacade testedCrucibleServerFacade;

	protected void setUp() throws Exception {
		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(httpServer);
		ConfigurationFactory.setConfiguration(createCrucibleTestConfiguration(mockBaseUrl, true));

		testedCrucibleServerFacade = CrucibleServerFactory.getCrucibleServerFacade();
	}

	private static PluginConfiguration createCrucibleTestConfiguration(String serverUrl, boolean isPassInitialized) {
		CrucibleConfigurationBean configuration = new CrucibleConfigurationBean();

		Collection<ServerBean> servers = new ArrayList<ServerBean>();
		ServerBean server = new ServerBean();

		server.setName("TestServer");
		server.setUrlString(serverUrl);
		server.setUserName(USER_NAME);

		server.setPasswordString(isPassInitialized ? PASSWORD : "", isPassInitialized);
		server.setIsConfigInitialized(isPassInitialized);
		servers.add(server);

		configuration.setServersData(servers);
		PluginConfigurationBean pluginConfig = new PluginConfigurationBean();
		pluginConfig.setCrucibleConfigurationData(configuration);

		return pluginConfig;
	}

	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		httpServer.stop();

		testedCrucibleServerFacade = null;
	}

	public void testFailedLoginGetAllReviews() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		Server server = ConfigurationFactory.getConfiguration().getProductServers(ServerType.CRUCIBLE_SERVER).getServers().iterator().next();
		try {
			testedCrucibleServerFacade.getAllReviews(server);
			fail();
		} catch (RemoteApiLoginFailedException e) {

		}

		mockServer.verify();
	}

	public void testConnectionTestSucceed() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		testedCrucibleServerFacade.testServerConnection(mockBaseUrl, USER_NAME, PASSWORD);
		mockServer.verify();
	}

	public void testConnectionTestFailed() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			testedCrucibleServerFacade.testServerConnection(mockBaseUrl, USER_NAME, PASSWORD);
			fail();
		} catch (RemoteApiLoginFailedException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testConnectionTestFailedNullUser() throws Exception {
		try {
			testedCrucibleServerFacade.testServerConnection(mockBaseUrl, null, PASSWORD);
			fail();
		} catch (RemoteApiLoginException e) {
			// expected
		}
	}

	public void testConnectionTestFailedNullPassword() throws Exception {
		try {
			testedCrucibleServerFacade.testServerConnection(mockBaseUrl, USER_NAME, null);
			fail();
		} catch (RemoteApiLoginException e) {
			// expected
		}
	}

	public void testConnectionTestFailedEmptyUrl() throws Exception {

		try {
			testedCrucibleServerFacade.testServerConnection("", USER_NAME, PASSWORD);
			fail();
		} catch (RemoteApiMalformedUrlException e) {
			// expected
		}
		mockServer.verify();
	}
}