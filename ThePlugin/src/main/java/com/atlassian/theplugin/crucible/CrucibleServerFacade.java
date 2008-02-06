package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.crucible.api.CrucibleLoginException;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:22:10
 * To change this template use File | Settings | File Templates.
 */
public interface CrucibleServerFacade {
	public void testServerConnection(String serverUrl, String userName, String password) throws CrucibleLoginException;
}
