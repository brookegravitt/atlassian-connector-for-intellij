package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.crucible.api.CrucibleSession;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:27:35
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleServerFacadeImpl implements CrucibleServerFacade {
	private CrucibleSession crucibleSession = null;

	public CrucibleServerFacadeImpl () {
		//crucibleSession = session;
	}

	public void testServerConnection(String serverUrl, String userName, String password) throws CrucibleException {
		CrucibleSession session = crucibleSession;

		if (session == null) {
			session = new CrucibleSession(serverUrl);
		}
		
		session.login(userName, password);
		session.logout();
	}
}
