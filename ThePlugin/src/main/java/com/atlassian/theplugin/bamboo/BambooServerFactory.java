package com.atlassian.theplugin.bamboo;

public final class BambooServerFactory {
	private static BambooServerFacade facade = new BambooServerFacadeImpl();

	private BambooServerFactory() {
	}

	public static BambooServerFacade getBambooServerFacade() {
		return facade;
	}
}
