package com.atlassian.theplugin.commons.fisheye.api.rest;

import com.atlassian.theplugin.commons.cfg.FishEyeServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.FishEyeServer;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacadeImpl;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.crucible.api.rest.CharArrayEquals;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import java.util.Arrays;
import java.util.Collection;

/**
 * User: pmaruszak
 */
public class FishEyeServerFacadeTest extends TestCase {

	private static final String USER_NAME = "myname";
	private static final String PASSWORD = "mypassword";
	private String URL = "http://localhost:9001";
	private FishEyeSession fishEyeSessionMock;
	private FishEyeServerFacadeImpl facade;


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		  ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

        fishEyeSessionMock = createMock(FishEyeSession.class);

        facade = new FishEyeServerFacadeImpl(){

			@Override
			public FishEyeSession getSession(final String url) throws RemoteApiMalformedUrlException {
				return fishEyeSessionMock;
			}
		};
	}


	
	public static Throwable eqException(char[] in) {
		EasyMock.reportMatcher(new CharArrayEquals(in));
		return null;
	}

	public static char[] charArrayContains(char[] expectedCharArray)
	{
	  EasyMock.reportMatcher(new CharArrayEquals(expectedCharArray));
	  return null;
	}

	public void testGetRepositories() throws ServerPasswordNotProvidedException, RemoteApiException {
		FishEyeServer server = prepareServerBean();

		facade.getSession(URL);
		//fishEyeSessionMock.login(server.getUsername(), PASSWORD.toCharArray());
		fishEyeSessionMock.login(EasyMock.eq(server.getUsername()), charArrayContains(PASSWORD.toCharArray()));

		
		fishEyeSessionMock.getRepositories();
		EasyMock.expectLastCall().andReturn(Arrays.asList(prepareRepositoryData(0), prepareRepositoryData(1)));
		fishEyeSessionMock.logout();

		replay(fishEyeSessionMock);

		// test call		
		Collection<String> ret = facade.getRepositories(server);
		assertEquals(2, ret.size());

		int i=0;
		for (String repoName: ret){
			assertEquals("RepoName" + i++, repoName);
		}		
		EasyMock.verify(fishEyeSessionMock);
	}


	private String prepareRepositoryData(final int i) {        
		return "RepoName" + i;

	}

	 private FishEyeServer prepareServerBean() {
        FishEyeServerCfg server = new FishEyeServerCfg("myname", new ServerId());
        server.setUrl(URL);
        server.setUsername(USER_NAME);
        server.setPassword(PASSWORD);
		server.setPasswordStored(false);
        return server;
    }
}
