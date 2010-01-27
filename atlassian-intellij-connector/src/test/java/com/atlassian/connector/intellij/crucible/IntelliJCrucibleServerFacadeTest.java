package com.atlassian.connector.intellij.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallback;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCacheImpl;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.mockito.Mockito;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

/**
 * @author Wojciech Seliga
 */
public class IntelliJCrucibleServerFacadeTest extends TestCase {
	private final User U1 = new User("wseliga", "Wojciech Seliga");
	private final User U2 = new User("pmaruszak", "Piotr Maruszak");
	private final User U3 = new User("jgorycki", "Janusz Gorycki");
	private final CrucibleProject P1 = new CrucibleProject("1", "PRJ1", "Project One", MiscUtil.buildArrayList("wseliga", "pmaruszak"));
	private final CrucibleProject P2 = new CrucibleProject("2", "PRJ2", "Project Two", MiscUtil.buildArrayList("wseliga"));

	public void testGetAllowedReviewers() throws RemoteApiException, ServerPasswordNotProvidedException {
		final CrucibleServerCfg SERVER_CFG = new CrucibleServerCfg(true, "myname", new ServerIdImpl());
		SERVER_CFG.setUrl("http://localhost");
		final ServerData SERVER = new ServerData(SERVER_CFG, new UserCfg("username", "password"));

		final CrucibleSession crucibleSessionMock = Mockito.mock(CrucibleSession.class);
		Mockito.when(crucibleSessionMock.getProjects()).thenReturn(MiscUtil.buildArrayList(P1, P2));
		Mockito.when(crucibleSessionMock.getUsers()).thenReturn(MiscUtil.buildArrayList(U1, U2, U3));

		final IntelliJCrucibleServerFacade facade = new IntelliJCrucibleServerFacade(
				new CrucibleServerFacadeImpl(LoggerImpl.getInstance(), new CrucibleUserCacheImpl(), new IntelliJHttpSessionCallback()) {
			@Override
			public CrucibleSession getSession(final ConnectionCfg server)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				return crucibleSessionMock;
			}
		});

		assertNull(facade.getAllowedReviewers(SERVER, "PRJ"));
		TestUtil.assertHasOnlyElements(facade.getAllowedReviewers(SERVER, P1.getKey()), U1, U2);
		TestUtil.assertHasOnlyElements(facade.getAllowedReviewers(SERVER, P2.getKey()), U1);
	}
}
