package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 16, 2008
 * Time: 10:44:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class SubscribedPlanBean implements SubscribedPlan {
    private Server server;
    private String planId;

    public Server getServer() {
        return server;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPlanId() {
        return planId;//"TP-DEF", "TP-TEST", "APITEST-DEVAPI"
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }
}
