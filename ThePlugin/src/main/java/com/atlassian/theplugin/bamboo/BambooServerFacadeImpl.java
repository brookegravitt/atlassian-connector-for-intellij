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

import java.util.*;

/**
 * Class used for communication wiht Bamboo Server.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:12:27 PM
 */
public class BambooServerFacadeImpl implements BambooServerFacade {
	private Map<String, BambooSession> sessions = new HashMap<String, BambooSession>();

	private static final Category LOG = Logger.getInstance(BambooServerFacadeImpl.class);

	public BambooServerFacadeImpl() {
	}

	private BambooSession getSession(Server server) throws BambooLoginException {

		BambooSession session = sessions.get(server.getUrlString());
		if (session == null) {
			session = new BambooSession(server.getUrlString());
			session.login(server.getUserName(), server.getPasswordString().toCharArray());
			sessions.put(server.getUrlString(), session);
		}
		return session;
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
		try {
			return getSession(bambooServer).listProjectNames();
		} catch (BambooException e) {
			LOG.info("Bamboo exception: " + e.getMessage());
			return null;
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
		try {
			BambooSession api = getSession(bambooServer);
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
			LOG.info("Bamboo exception: " + e.getMessage());
			return null;
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

		String connectionErrorMessage;
		BambooSession api = null;
		try {
			api = getSession(bambooServer);
			connectionErrorMessage = "";
		} catch (BambooLoginFailedException e) {
			if (bambooServer.getIsConfigInitialized()) {
				LOG.info("Bamboo login exception: " + e.getMessage());
				connectionErrorMessage = e.getMessage();
			} else {
				throw new ServerPasswordNotProvidedException();
			}
		} catch (BambooLoginException e) {
			LOG.info("Bamboo login exception: " + e.getMessage());
			connectionErrorMessage = e.getMessage();
		}

		Collection<BambooPlan> plansForServer = getPlanList(bambooServer);

		if (bambooServer.getUseFavourite()) {
			if (plansForServer != null) {
				for (BambooPlan bambooPlan : plansForServer) {
					if (bambooPlan.isFavourite()) {
						if (api != null && api.isLoggedIn()) {
							BambooBuild buildInfo = api.getLatestBuildForPlan(bambooPlan.getPlanKey());
							((BambooBuildInfo) buildInfo).setEnabled(bambooPlan.isEnabled());
							builds.add(buildInfo);
						} else {
							builds.add(constructBuildErrorInfo(
									bambooServer.getUrlString(),
									bambooPlan.getPlanKey(),
									connectionErrorMessage));
						}
					}
				}
			}
		} else {
			for (SubscribedPlan plan : bambooServer.getSubscribedPlans()) {
				if (api != null && api.isLoggedIn()) {
					BambooBuild buildInfo = api.getLatestBuildForPlan(plan.getPlanId());
					((BambooBuildInfo) buildInfo).setEnabled(true);
					for (BambooPlan bambooPlan : plansForServer) {
						if (plan.getPlanId().equals(bambooPlan.getPlanKey())) {
							((BambooBuildInfo) buildInfo).setEnabled(bambooPlan.isEnabled());
						}
					}
					builds.add(buildInfo);
				} else {
					builds.add(constructBuildErrorInfo(bambooServer.getUrlString(), plan.getPlanId(), connectionErrorMessage));
				}
			}
		}


		return builds;
	}

	public BuildDetails getBuildDetails(Server bambooServer, String buildKey, String buildNumber)
			throws ServerPasswordNotProvidedException {
		try {
			BambooSession api = getSession(bambooServer);
			return api.getBuildResultDetails(buildKey, buildNumber);
		} catch (BambooException e) {
			LOG.info("Bamboo exception: " + e.getMessage());
			return null;
		}
	}

	public void addLabelToBuild(Server bambooServer, String buildKey, String buildNumber, String buildLabel)
			throws ServerPasswordNotProvidedException {
		try {
			BambooSession api = getSession(bambooServer);
			api.addLabelToBuild(buildKey, buildNumber, buildLabel);
		} catch (BambooException e) {
			LOG.info("Bamboo exception: " + e.getMessage());
		}
	}

	public void addCommentToBuild(Server bambooServer, String buildKey, String buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException {
		try {
			BambooSession api = getSession(bambooServer);
			api.addCommentToBuild(buildKey, buildNumber, buildComment);
		} catch (BambooException e) {
			LOG.info("Bamboo exception: " + e.getMessage());
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
	public Collection<String> getFavouritePlans(Server bambooServer) throws ServerPasswordNotProvidedException {
		try {
			return getSession(bambooServer).getFavouriteUserPlans();
		} catch (BambooException e) {
			LOG.info("Bamboo exception: " + e.getMessage());
			return null;
		}
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
