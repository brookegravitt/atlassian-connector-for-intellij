package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:13:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooConfigurationBean implements BambooConfiguration {
    private ServerBean server = new ServerBean();
    private Collection<SubscribedPlanBean> subscribedPlans = new ArrayList<SubscribedPlanBean>();


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

    public Collection<SubscribedPlan> getSubscribedPlans() {
        //TODO: mock implementation
        Collection<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
        SubscribedPlanBean sb1 = new SubscribedPlanBean();
        sb1.setPlanId("TP-TEST");
        sb1.setServer(server);
        plans.add(sb1);
        return plans;
    }

    //TODO: Stuff below is for convenience only, it should disappear once a proper multi server config is ready
    @Transient
    public String getServerName() {
        return server.getName();
    }

    public void setServerName(final String serverName) {
        server = new ServerBean(server);
        server.setName(serverName);
    }

    @Transient
    public String getServerUrl() {
        return server.getUrlString();
    }

    public void setServerUrl(final String serverUrl) {
        server = new ServerBean(server);
        server.setUrlString(serverUrl);
    }

    @Transient
    public String getUsername() {
        return server.getUsername();
    }

    public void setUsername(final String username) {
        server = new ServerBean(server);
        server.setUsername(username);
    }

    @Transient
    public String getPassword() {
        return server.getPassword();
    }

    public void setPassword(final String password) {
        server = new ServerBean(server);
        server.setPassword(password);
    }
}
