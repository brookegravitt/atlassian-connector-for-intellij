package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.api.BambooException;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.bamboo.api.BambooSession;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
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
	 * List projects defined on Bamboo server.
	 *
	 * @return list of projects or null on error
	 */
	public Collection<BambooProject> getProjectList() throws ServerPasswordNotProvidedException {
		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();

		BambooSession api = new BambooSession(server.getUrlString());
		try {
			api.login(server.getUsername(), server.getPasswordString().toCharArray());
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
	public Collection<BambooPlan> getPlanList() throws ServerPasswordNotProvidedException {
		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();

		BambooSession api = new BambooSession(server.getUrlString());
		try {
			api.login(server.getUsername(), server.getPasswordString().toCharArray());
			return api.listPlanNames();
		} catch (BambooException e) {
			LOG.error("Bamboo exception: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Returns build results of subscribed plans.
	 * <p>
	 * Method iterates through configured Bamboo servers and retrieves build info in the form of {@link BambooBuild}
	 * objects.<p>
	 * The build result may include failure description if the status cannot be retrieved.
	 *
	 * @return all subscribed plan build results.
	 * @throws ServerPasswordNotProvidedException when the password for a server has not been stored in configuration or
	 * 			provided by the user.
	 */
	public Collection<BambooBuild> getSubscribedPlansResults() throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();
		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();

		BambooSession api = new BambooSession(server.getUrlString());
		String connectionErrorMessage;
		try {
			assert(server != null);
			assert(server.getUsername() != null);
			assert(server.getPasswordString() != null);


			api.login(server.getUsername(), server.getPasswordString().toCharArray());
			connectionErrorMessage = "";
		} catch (BambooLoginException e) {
			LOG.error("Bamboo login exception: " + e.getMessage());
			connectionErrorMessage = e.getMessage();
		}

		for (SubscribedPlan plan : server.getSubscribedPlans()) {
			if (api.isLoggedIn()) {
				BambooBuild buildInfo = api.getLatestBuildForPlan(plan.getPlanId());
				builds.add(buildInfo);
			} else {
				builds.add(constructBuildErrorInfo(server.getUrlString(), plan.getPlanId(), connectionErrorMessage));
			}
		}

		if (api.isLoggedIn()) {
			api.logout();
		}

		return builds;
	}

	BambooBuild constructBuildErrorInfo(String serverUrl, String planId, String message) {
		BambooBuildInfo buildInfo = new BambooBuildInfo();

		buildInfo.setServerUrl(serverUrl);
		buildInfo.setBuildKey(planId);
		buildInfo.setBuildState(BuildStatus.ERROR.toString());
		buildInfo.setMessage(message);
		buildInfo.setPollingTime(new Date());

		return buildInfo;
	}


}
