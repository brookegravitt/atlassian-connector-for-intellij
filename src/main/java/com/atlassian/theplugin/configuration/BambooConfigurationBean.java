package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:13:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooConfigurationBean implements BambooConfiguration {
    private ServerBean server = new ServerBean();

    /**
     * For storage purposes.
     *
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public ServerBean getServerData() {
        return server;
    }

    /**
     * For storage purposes.
     *
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public void setServerData(ServerBean server) {
        this.server = server;
    }


    /**
     * Implemnentation for the interface.
     *
     * Do not mistake for #getServerData()
     * 
     * @return
     */
    @Transient
    public Server getServer() {
        return server;
    }


}
