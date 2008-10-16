package com.atlassian.theplugin.commons.fisheye.api.rest;

import com.atlassian.theplugin.commons.cfg.FishEyeServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
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
import java.util.List;

/**
 * User: pmaruszak
 */
public class FishEyeServerFacadeTest extends TestCase {

	private static final String USER_NAME = "myname";
	private static final String PASSWORD = "mypassword";
	private String URL = "http://localhost:9001";
	private FishEyeSession fishEyeSessionMock;
	private FishEyeServerFacadeImpl facade;



	protected void setUp() throws Exception {
		super.setUp();
		  ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

        fishEyeSessionMock = createMock(FishEyeSession.class);

        facade = new FishEyeServerFacadeImpl(){

			public FishEyeSession getSession(final String url) throws RemoteApiMalformedUrlException {
				return fishEyeSessionMock;
			}
		};
	}


	
	public static Throwable eqException(char[] in) {
		EasyMock.reportMatcher(new CharArrayEquals(in));
		return null;
	}
	class ComparableCharA implements Comparable<char[]> {

		char[] value;
		ComparableCharA(char[] initialValue){
			this.value = initialValue;
		}
		public int compareTo(final char[] chars) {
			return 0;  
		}
	};

	public static char[] charArrayContains(char[] expectedCharArray)
	{
	  EasyMock.reportMatcher(new CharArrayEquals(expectedCharArray));
	  return null;
	}

	public void testGetRepositories() throws ServerPasswordNotProvidedException, RemoteApiException {
		FishEyeServerCfg server = prepareServerBean();

		facade.getSession(URL);
		//fishEyeSessionMock.login(server.getUsername(), PASSWORD.toCharArray());
		fishEyeSessionMock.login(EasyMock.eq(server.getUsername()), charArrayContains(PASSWORD.toCharArray()));

		
		fishEyeSessionMock.getRepositories();
		EasyMock.expectLastCall().andReturn(Arrays.asList(prepareRepositoryData(0), prepareRepositoryData(1)));
		fishEyeSessionMock.logout();

		replay(fishEyeSessionMock);

		// test call		
		List<String> ret = facade.getRepositories(server);
		assertEquals(2, ret.size());
		for (int i = 0; i < 2; i++) {
			String id = Integer.toString(i);
			assertEquals("RepoName" + id, ret.get(i));
		}
		EasyMock.verify(fishEyeSessionMock);
	}


	private String prepareRepositoryData(final int i) {        
		return "RepoName" + Integer.toString(i);

	}

	 private FishEyeServerCfg prepareServerBean() {
        FishEyeServerCfg server = new FishEyeServerCfg("myname", new ServerId());
        server.setUrl(URL);
        server.setUsername(USER_NAME);
        server.setPassword(PASSWORD);
		server.setPasswordStored(false);
        return server;
    }
}
