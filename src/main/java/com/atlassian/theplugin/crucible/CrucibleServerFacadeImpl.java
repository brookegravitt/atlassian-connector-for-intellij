package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.crucible.api.CrucibleSessionImpl;
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
		//crucibleSessionImpl = session;
	}

	/**
	 *
	 * @param serverUrl @see com.atlassian.theplugin.crucible.api.CrucibleSessionImpl#constructor(String baseUrl)
	 * @param userName
	 * @param password
	 * @throws CrucibleException
	 */
	public void testServerConnection(String serverUrl, String userName, String password) throws CrucibleException {
		CrucibleSession session = crucibleSession;

		if (session == null) {
			session = new CrucibleSessionImpl(serverUrl);
		}
		
		session.login(userName, password);
		session.logout();
	}

	public void createReview() {
		//To change body of implemented methods use File | Settings | File Templates.
	}


	/**
	 * Used only for tests purposes. Should not be used manually but only for tests injections.
	 * @param crucibleSession
	 */
	public void setCrucibleSession(CrucibleSession crucibleSession) {
		this.crucibleSession = crucibleSession;
	}
}
