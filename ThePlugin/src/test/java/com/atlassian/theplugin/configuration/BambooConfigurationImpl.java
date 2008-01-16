package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.BambooConfiguration;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Initial (dummy) implementation of bamboo config
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 12:00:32 PM
 */
class BambooConfigurationImpl implements BambooConfiguration {
    ServerBean server = new ServerBean();

    public Server getServer() {
        return server;
    }

    public Collection<SubscribedPlan> getSubscribedPlans() {
        Collection<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
        SubscribedPlanBean sb1 = new SubscribedPlanBean();
        sb1.setPlanId("TP-TEST");
        plans.add(sb1);

        return plans;
    }

    public void setServer(ServerBean newConfiguration) {
        server = new ServerBean(newConfiguration);
    }
}
