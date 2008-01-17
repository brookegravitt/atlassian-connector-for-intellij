package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.bamboo.BambooProjectInfo;
import com.atlassian.theplugin.bamboo.BambooPlanInfo;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.SubscribedPlan;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.bamboo.api.RestApi;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.bamboo.api.BambooException;

import java.util.Collection;
import java.util.ArrayList;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:12:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooServerImpl implements BambooServerFacade {

    private final static Category log = Logger.getInstance(BambooServerImpl.class);
    
    public BambooServerImpl() {
    }

    public Boolean testServerConnection(String url, String userName, String password) throws BambooLoginException {
        RestApi apiHandler = RestApi.login(url, userName, password);
        if (apiHandler != null) {
            apiHandler.logout();
        }
        return true;
    }

    public Collection<BambooProject> getProjectList() {
        Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();

        RestApi api = null;
        try {
            api = RestApi.login(server.getUrlString(), server.getUsername(), server.getPassword());
            return api.listProjectNames();
        } catch (BambooException e) {
            log.error("Bamboo exception: " + e.getMessage());
        }
        return null;
    }

    public Collection<BambooPlan> getPlanList() {
        Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();

        RestApi api = null;
        try {
            api = RestApi.login(server.getUrlString(), server.getUsername(), server.getPassword());
            return api.listPlanNames();
        } catch (BambooException e) {
            log.error("Bamboo exception: " + e.getMessage());
        }
        return null;
    }

    public Collection<BambooBuild> getSubscribedPlansResults() {        
        Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

        Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();

        RestApi api = null;
        String connectionErrorMessage = null;
        try {
            api = RestApi.login(server.getUrlString(), server.getUsername(), server.getPassword());
        } catch (BambooLoginException e) {
            log.error("Bamboo login exception: " + e.getMessage());
            connectionErrorMessage = e.getMessage();
        }

        for (SubscribedPlan plan : server.getSubscribedPlans()) {
            if (api != null) {
                try {
                    builds.add(api.getLatestBuildForPlan(plan.getPlanId()));                
                } catch (BambooException e) {
                    log.error("Bamboo exception: " + e.getMessage());
                    builds.add(getErrorBuildInfo(plan.getPlanId(), e.getMessage()));
                }
            } else {
                builds.add(getErrorBuildInfo(plan.getPlanId(), connectionErrorMessage));
            }
        }

        if (api != null) {
            api.logout();
        }

        return builds;
    }
    
    private BambooBuild getErrorBuildInfo(String planId, String message) {
        BambooBuildInfo buildInfo = new BambooBuildInfo();
        buildInfo.setBuildKey(planId);
        buildInfo.setBuildState(BuildStatus.ERROR.toString());
        buildInfo.setMessage(message);

        return buildInfo;
    }
}
