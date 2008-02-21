package com.atlassian.theplugin.bamboo;

public final class BambooServerFactory {
	private static BambooServerFacade facade = new BambooServerFacadeImpl();

	///CLOVER:ON
	private BambooServerFactory() {
	}
	///CLOVER:ON
	
	public static BambooServerFacade getBambooServerFacade() {
		return facade;
	}
}
