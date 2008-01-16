package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.bamboo.BambooProjectInfo;
import com.atlassian.theplugin.bamboo.BambooPlanInfo;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.SubscribedPlan;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.api.bamboo.RestApi;
import com.atlassian.theplugin.api.bamboo.BambooLoginException;
import com.atlassian.theplugin.api.bamboo.BambooException;

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

    public Collection<BambooProject> getProjectList() {
        Collection<BambooProject> newProject = new ArrayList<BambooProject>();
        newProject.add(new BambooProjectInfo("The Plugin", "TP"));
        newProject.add(new BambooProjectInfo("API Test Project", "APITEST"));
        return newProject;
    }

    public Collection<BambooPlan> getPlanList() {
        Collection<BambooPlan> newProject = new ArrayList<BambooPlan>();
        newProject.add(new BambooPlanInfo("Default", "TP-DEF"));
        newProject.add(new BambooPlanInfo("TestPlan", "TP-TEST"));
        newProject.add(new BambooPlanInfo("Default", "APITEST-DEF"));
        return newProject;
    }




    public Collection<BambooBuild> getSubscribedPlansResults() {        
        Collection<? extends SubscribedPlan> plans = ConfigurationFactory.getConfiguration().getBambooConfiguration().getSubscribedPlans();
        Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

        for (SubscribedPlan plan : plans) {
            Server server = plan.getServer();
            try {
                RestApi api = RestApi.login(server.getUrlString(), server.getUsername(), server.getPassword());
                builds.add(api.getLatestPlanBuild(plan.getPlanId()));
            } catch (BambooLoginException e) {
                log.error("Bamboo login exception: " + e.getMessage());
                builds.add(getErrorBuildInfo(plan.getPlanId(), e.getMessage()));
            } catch (BambooException e) {
                log.error("Bamboo exception: " + e.getMessage());
                builds.add(getErrorBuildInfo(plan.getPlanId(), e.getMessage()));                
            }
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


    public BambooBuild getLatestBuildForPlan(String planName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }    
}
