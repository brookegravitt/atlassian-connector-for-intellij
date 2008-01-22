package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.api.BambooException;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.bamboo.api.RestApi;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.SubscribedPlan;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:12:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooServerFacadeImpl implements BambooServerFacade {

    private final static Category log = Logger.getInstance(BambooServerFacadeImpl.class);
    
    public BambooServerFacadeImpl() {
    }

    /**
     * Test connection to Bamboo server
     * @param url
     * @param userName
     * @param password
     * @throws BambooLoginException on failed login
     */
    public void testServerConnection(String url, String userName, String password) throws BambooLoginException {
        RestApi apiHandler = RestApi.login(url, userName, password);
        apiHandler.logout();  
    }

    /**
     * List projects defined on Bamboo server
     * @return list of projects or null on error
     */
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

    /**
     * List plans defined on Bamboo server
     * @return list of plans or null on error
     */
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

    /**
     * List details on subscribed plans
     * @return results on subscribed builds
     */
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
            try {
                api.logout();
            } catch (BambooLoginException e) {
                log.error("Bamboo login exception: " + e.getMessage());
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
}
