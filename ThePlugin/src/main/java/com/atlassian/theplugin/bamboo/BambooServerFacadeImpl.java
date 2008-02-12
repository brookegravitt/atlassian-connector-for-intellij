package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.api.BambooException;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.bamboo.api.BambooLoginFailedException;
import com.atlassian.theplugin.bamboo.api.BambooSession;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.SubscribedPlan;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
	 * @param url	  Bamboo server base URL
	 * @param userName Bamboo user name
	 * @param password Bamboo password
	 * @throws BambooLoginException on failed login
	 * @see BambooLoginFailedException
	 */
	public void testServerConnection(String url, String userName, String password) throws BambooLoginException {
		BambooSession apiHandler = new BambooSession(url);
		apiHandler.login(userName, password.toCharArray());
		apiHandler.logout();
	}

	/**
	 * List projects defined on Bamboo server.
	 *
	 * @param bambooServer Bamboo server information
	 * @return list of projects or null on error
	 * @throws ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 */
	public Collection<BambooProject> getProjectList(Server bambooServer) throws ServerPasswordNotProvidedException {

		BambooSession api = new BambooSession(bambooServer.getUrlString());
		try {
			api.login(bambooServer.getUsername(), bambooServer.getPasswordString().toCharArray());
			return api.listProjectNames();
		} catch (BambooException e) {
			LOG.error("Bamboo exception: " + e.getMessage());
			return null;
		} finally {
			api.logout();
		}
	}

	/**
	 * List plans defined on Bamboo server.
	 *
	 * @param bambooServer Bamboo server information
	 * @return list of plans or null on error
	 * @throws ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 */
	public Collection<BambooPlan> getPlanList(Server bambooServer) throws ServerPasswordNotProvidedException {
		BambooSession api = new BambooSession(bambooServer.getUrlString());
		try {
			api.login(bambooServer.getUsername(), bambooServer.getPasswordString().toCharArray());
			List<BambooPlan> plans = api.listPlanNames();
			List<String> favPlans = api.getFavouriteUserPlans();

			for (String fav : favPlans) {
				for (BambooPlan plan : plans) {
					if (plan.getPlanKey().equalsIgnoreCase(fav)) {
						((BambooPlanData) plan).setFavourite(true);
						break;
					}
				}
			}
			return plans;
		} catch (BambooException e) {
			LOG.error("Bamboo exception: " + e.getMessage());
			return null;
		} finally {
			api.logout();
		}
	}

	/**
	 * List details on subscribed plans.<p>
	 * <p/>
	 * Returns info on all subscribed plans including information about failed attempt.<p>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer Bamboo server information
	 * @return results on subscribed builds
	 * @throws ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.bamboo.api.BambooSession#login(String, char[])
	 */
	public Collection<BambooBuild> getSubscribedPlansResults(Server bambooServer) throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();
		BambooSession api = new BambooSession(bambooServer.getUrlString());
		String connectionErrorMessage;
		try {
			api.login(bambooServer.getUsername(), bambooServer.getPasswordString().toCharArray());
			connectionErrorMessage = "";
		} catch (BambooLoginFailedException e) {
			if (bambooServer.getIsConfigInitialized()) {
				LOG.error("Bamboo login exception: " + e.getMessage());
				connectionErrorMessage = e.getMessage();
			} else {
				throw new ServerPasswordNotProvidedException();
			}

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


	private BambooBuild constructBuildErrorInfo(String serverUrl, String planId, String message) {
		BambooBuildInfo buildInfo = new BambooBuildInfo();

		buildInfo.setServerUrl(serverUrl);
		buildInfo.setBuildKey(planId);
		buildInfo.setBuildState(BuildStatus.UNKNOWN.toString());
		buildInfo.setMessage(message);
		buildInfo.setPollingTime(new Date());

		return buildInfo;
	}

}
