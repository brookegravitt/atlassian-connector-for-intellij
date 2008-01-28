package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.api.BambooException;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.bamboo.api.BambooSession;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.SubscribedPlan;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Class used for communication wiht Bamboo Server.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:12:27 PM
 */
public class BambooServerFacadeImpl implements BambooServerFacade {

	private static final Category LOG = Logger.getInstance(BambooServerFacadeImpl.class);

	public BambooServerFacadeImpl() {
	}

	/**
	 * Test connection to Bamboo server.
	 *
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
	 * @param bambooServer
     * @return list of projects or null on error
     */
	public Collection<BambooProject> getProjectList(Server bambooServer) throws ServerPasswordNotProvidedException {

		BambooSession api = new BambooSession(bambooServer.getUrlString());
		try {
			api.login(bambooServer.getUsername(), bambooServer.getPasswordString().toCharArray());
			return api.listProjectNames();
		} catch (BambooException e) {
			LOG.error("Bamboo exception: " + e.getMessage());
			return null;
		}
	}

	/**
	 * List plans defined on Bamboo server
	 *
	 * @return list of plans or null on error
	 */
	public Collection<BambooPlan> getPlanList(Server bambooServer) throws ServerPasswordNotProvidedException {


		BambooSession api = new BambooSession(bambooServer.getUrlString());
		try {
			api.login(bambooServer.getUsername(), bambooServer.getPasswordString().toCharArray());
			return api.listPlanNames();
		} catch (BambooException e) {
			LOG.error("Bamboo exception: " + e.getMessage());
			return null;
		}
	}

	/**
	 * List details on subscribed plans
	 *
	 * @return results on subscribed builds
	 */
	public Collection<BambooBuild> getSubscribedPlansResults(Server bambooServer) throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();
        assert(bambooServer != null);
		BambooSession api = new BambooSession(bambooServer.getUrlString());
		String connectionErrorMessage;
		try {
			assert(bambooServer.getUsername() != null);
			assert(bambooServer.getPasswordString() != null);

			api.login(bambooServer.getUsername(), bambooServer.getPasswordString().toCharArray());
			connectionErrorMessage = "";
		} catch (BambooLoginException e) {
			LOG.error("Bamboo login exception: " + e.getMessage());
			connectionErrorMessage = e.getMessage();
		}

		for (SubscribedPlan plan : bambooServer.getSubscribedPlans()) {
			if (api.isLoggedIn()) {
				BambooBuild buildInfo = api.getLatestBuildForPlan(plan.getPlanId());
				builds.add(buildInfo);
			} else {
				builds.add(constructBuildErrorInfo(bambooServer.getUrlString(), plan.getPlanId(), connectionErrorMessage));
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
		buildInfo.setBuildState(BuildStatus.UNKNOWN.toString());
		buildInfo.setMessage(message);

		return buildInfo;
	}

	BambooBuild constructBuildErrorInfo(String serverUrl, String planId, String message) {
		BambooBuildInfo buildInfo = new BambooBuildInfo();

		buildInfo.setServerUrl(serverUrl);
		buildInfo.setBuildKey(planId);
		buildInfo.setBuildState(BuildStatus.UNKNOWN.toString());
		buildInfo.setMessage(message);
		buildInfo.setPollingTime(new Date());

		return buildInfo;
	}

}
