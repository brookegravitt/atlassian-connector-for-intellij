package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * BambooConfigurationBean for a single Bamboo server.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:51:08 AM
 */
public class ServerBean implements Server {
    private String name;
    private String urlString;
    private String username;
    private String password;

    private List<SubscribedPlanBean> subscribedPlans = new ArrayList<SubscribedPlanBean>();

    ServerBean(ServerBean s) {
        name = s.getName();
        urlString = s.getUrlString();
        username = s.getUsername();
        password = s.getPassword();

        subscribedPlans = new ArrayList<SubscribedPlanBean>(s.getSubscribedPlansData());
    }

    public ServerBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Transient
    public Collection<? extends SubscribedPlan> getSubscribedPlans() {
        return subscribedPlans;
    }

    public List<SubscribedPlanBean> getSubscribedPlansData() {
        return subscribedPlans;
    }

    public void setSubscribedPlansData(List<SubscribedPlanBean> subscribedPlansData) {
        this.subscribedPlans = subscribedPlansData;
    }

}