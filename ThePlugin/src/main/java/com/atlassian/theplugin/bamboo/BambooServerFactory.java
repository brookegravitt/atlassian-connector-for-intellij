package com.atlassian.theplugin.bamboo;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:17:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooServerFactory {
    private static BambooServerFacade facade = new BambooServerImpl();

    private BambooServerFactory(){
    }

    public static BambooServerFacade getBambooServerFacade() {
        return facade;
    }
}
