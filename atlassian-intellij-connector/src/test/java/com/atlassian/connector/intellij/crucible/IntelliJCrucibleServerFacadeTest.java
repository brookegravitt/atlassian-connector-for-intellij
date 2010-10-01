package com.atlassian.connector.intellij.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCacheImpl;
import com.atlassian.theplugin.commons.crucible.api.model.ExtendedCrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;
import org.mockito.Mockito;

/**
 * @author Wojciech Seliga
 */
public class IntelliJCrucibleServerFacadeTest extends TestCase {
	private final User U1 = new User("wseliga", "Wojciech Seliga");
	private final User U2 = new User("pmaruszak", "Piotr Maruszak");
	private final User U3 = new User("jgorycki", "Janusz Gorycki");
	private final BasicProject P1 = new ExtendedCrucibleProject("1", "PRJ1", "Project One", MiscUtil.buildArrayList(
			"wseliga",
			"pmaruszak"));
	private final BasicProject P2 = new ExtendedCrucibleProject("2", "PRJ2", "Project Two",
			MiscUtil.buildArrayList("wseliga"));

	public void testGetAllowedReviewers() throws RemoteApiException, ServerPasswordNotProvidedException {
		final CrucibleServerCfg SERVER_CFG = new CrucibleServerCfg(true, "myname", new ServerIdImpl());
		SERVER_CFG.setUrl("http://localhost");
		final ServerData SERVER = new ServerData(SERVER_CFG, new UserCfg("username", "password"));

		final CrucibleSession crucibleSessionMock = Mockito.mock(CrucibleSession.class);
		Mockito.when(crucibleSessionMock.getProjects()).thenReturn(MiscUtil.buildArrayList(P1, P2));
		Mockito.when(crucibleSessionMock.getUsers()).thenReturn(MiscUtil.buildArrayList(U1, U2, U3));
        Mockito.when(crucibleSessionMock.getProject("PRJ1")).thenReturn((ExtendedCrucibleProject)null);


		final IntelliJCrucibleServerFacade facade = new IntelliJCrucibleServerFacade(
				new CrucibleServerFacadeImpl(LoggerImpl.getInstance(), new CrucibleUserCacheImpl(), new IntelliJHttpSessionCallbackImpl()) {
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
