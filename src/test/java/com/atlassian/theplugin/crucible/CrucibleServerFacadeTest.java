package com.atlassian.theplugin.crucible;

import junit.framework.TestCase;
import com.atlassian.theplugin.crucible.api.CrucibleException;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:54:14
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleServerFacadeTest extends TestCase {

	private CrucibleServerFacade facade;
	//private MockControl control;
	//private Collaborator collaborator;

	public void testConnectionTest() {

		//control = MockControl.createControl(CrucibleServerFacade.class);

		//facade = (CrucibleServerFacade) control.getMock();

		//facade

		CrucibleServerFacade fasade = CrucibleServerFactory.getCrucibleServerFacade();

		try {
			fasade.testServerConnection("http://lech.atlassian.pl:8060", "test", "d0n0tch@nge");
		} catch (CrucibleException e) {
			fail("testServerConnection failed");
		}
	}
}
