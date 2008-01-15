package com.atlassian.theplugin.bamboo;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:17:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooServerFactory {
    public BambooServerFacade getBambooServerFacade() {
        return new BambooServerImpl();
    }
}
