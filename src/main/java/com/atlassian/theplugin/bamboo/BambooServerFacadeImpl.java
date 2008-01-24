package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.api.BambooException;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.bamboo.api.BambooSession;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedExeption;
import com.atlassian.theplugin.configuration.SubscribedPlan;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class used for communication wiht Bamboo Server 
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:12:27 PM
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
        BambooSession apiHandler = new BambooSession(url);
        apiHandler.login(userName, password.toCharArray());
        apiHandler.logout();  
    }

    /**
     * List projects defined on Bamboo server
     * @return list of projects or null on error
     */
    public Collection<BambooProject> getProjectList() throws ServerPasswordNotProvidedExeption {
        Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();

        BambooSession api = new BambooSession(server.getUrlString());
        try {
            api.login(server.getUsername(), server.getPasswordString().toCharArray());
            return api.listProjectNames();
        } catch (BambooException e) {
            log.error("Bamboo exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * List plans defined on Bamboo server
     * @return list of plans or null on error
     */
    public Collection<BambooPlan> getPlanList() throws ServerPasswordNotProvidedExeption {
        Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();

        BambooSession api = new BambooSession(server.getUrlString());
        try {
            api.login(server.getUsername(), server.getPasswordString().toCharArray());
            return api.listPlanNames();
        } catch (BambooException e) {
            log.error("Bamboo exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * List details on subscribed plans
     * @return results on subscribed builds
     */
    public Collection<BambooBuild> getSubscribedPlansResults() throws ServerPasswordNotProvidedExeption {
        Collection<BambooBuild> builds = new ArrayList<BambooBuild>();
        Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();

        BambooSession api = new BambooSession(server.getUrlString());
        String connectionErrorMessage;
        try {
            api.login(server.getUsername(), server.getPasswordString().toCharArray());
            connectionErrorMessage = "";
        } catch (BambooLoginException e) {
            log.error("Bamboo login exception: " + e.getMessage());
            connectionErrorMessage = e.getMessage();
        }

        for (SubscribedPlan plan : server.getSubscribedPlans()) {
            if (api.isLoggedIn()) {
                try {
                    BambooBuildInfo buildInfo = api.getLatestBuildForPlan(plan.getPlanId());
                    buildInfo.setServerUrl(server.getUrlString());
                    builds.add(buildInfo);                
                } catch (BambooException e) {
                    log.error("Bamboo exception: " + e.getMessage());
                    builds.add(getErrorBuildInfo(server, plan.getPlanId(), e.getMessage()));
                }
            } else {
                builds.add(getErrorBuildInfo(server, plan.getPlanId(), connectionErrorMessage));
            }
        }

        if (api.isLoggedIn()) {
            api.logout();
        }

        return builds;
    }
    
    private BambooBuild getErrorBuildInfo(Server server, String planId, String message) {
        BambooBuildInfo buildInfo = new BambooBuildInfo();
        buildInfo.setServerUrl(server.getUrlString());
        buildInfo.setBuildKey(planId);
        buildInfo.setBuildState(BuildStatus.ERROR.toString());
        buildInfo.setMessage(message);

        return buildInfo;
    }
}
