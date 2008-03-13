package com.atlassian.theplugin.crucible;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:26:14
 * To change this template use File | Settings | File Templates.
 */
public final class CrucibleServerFactory {
	private static CrucibleServerFacade facade = new CrucibleServerFacadeImpl();

	///CLOVER:OFF
	private CrucibleServerFactory() {
	}
	///CLOVER:ON
	
	public static CrucibleServerFacade getCrucibleServerFacade() {
		return facade;
	}
}
