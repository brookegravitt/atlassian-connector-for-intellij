/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.bamboo.api.AutoRenewBambooSession;
import com.atlassian.theplugin.commons.bamboo.api.BambooSession;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * Class used for communication wiht Bamboo Server.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:12:27 PM
 */
public final class BambooServerFacadeImpl implements BambooServerFacade {
    private Map<String, BambooSession> sessions = new WeakHashMap<String, BambooSession>();
    private Logger loger;

    private static BambooServerFacadeImpl instance = null;

    private BambooServerFacadeImpl(Logger loger) {
        this.loger = loger;
    }                                                                                            

    public static BambooServerFacade getInstance(Logger loger) {
        if (instance == null) {
            instance = new BambooServerFacadeImpl(loger);
        }

        return instance;
    }

    public ServerType getServerType() {
        return ServerType.BAMBOO_SERVER;
    }

    private synchronized BambooSession getSession(BambooServerCfg server) throws RemoteApiException {
        // @todo old server will stay on map - remove them !!!
        String key = server.getUsername() + server.getUrl() + server.getPassword();
        BambooSession session = sessions.get(key);
        if (session == null) {
            session = new AutoRenewBambooSession(server.getUrl());
            sessions.put(key, session);
        }
        if (!session.isLoggedIn()) {
            session.login(server.getUsername(), server.getPassword().toCharArray());
            try {
                if (session.getBamboBuildNumber() > 0) {
                    server.setIsBamboo2(true);
                } else {
                    server.setIsBamboo2(false);
                }
            } catch (RemoteApiException e) {
                // can not validate as Bamboo 2
                server.setIsBamboo2(false);
            }
        }
        return session;
    }

    /**
     * Test connection to Bamboo server.
     *
     * @param url      Bamboo server base URL
     * @param userName Bamboo user name
     * @param password Bamboo password
     * @throws RemoteApiException on failed login
     * @see RemoteApiLoginFailedException
     */
    public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
        BambooSession apiHandler = new AutoRenewBambooSession(url);
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
    public Collection<BambooProject> getProjectList(BambooServerCfg bambooServer) throws ServerPasswordNotProvidedException
            , RemoteApiException {
        try {
            return getSession(bambooServer).listProjectNames();
        } catch (RemoteApiException e) {
            loger.error("Bamboo exception: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * List plans defined on Bamboo server.
     *
     * @param bambooServer Bamboo server information
     * @return list of plans
     * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
     *          when invoked for Server that has not had the password set yet
     */
    public Collection<BambooPlan> getPlanList(BambooServerCfg bambooServer)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        BambooSession api = getSession(bambooServer);
        List<BambooPlan> plans = api.listPlanNames();
        try {
            List<String> favPlans = api.getFavouriteUserPlans();
            for (String fav : favPlans) {
                for (BambooPlan plan : plans) {
                    if (plan.getPlanKey().equalsIgnoreCase(fav)) {
                        ((BambooPlanData) plan).setFavourite(true);
                        break;
                    }
                }
            }
        } catch (RemoteApiException e) {
            // lack of favourite info is not a blocker here
        }
        return plans;
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
     * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
     *          when invoked for Server that has not had the password set yet
     * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
     */
    public Collection<BambooBuild> getSubscribedPlansResults(BambooServerCfg bambooServer)
            throws ServerPasswordNotProvidedException {
        Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

        String connectionErrorMessage;
        BambooSession api = null;
        try {
            api = getSession(bambooServer);
            connectionErrorMessage = "";
        } catch (RemoteApiLoginFailedException e) {
			// TODO wseliga used to be bambooServer.getIsConfigInitialized() here
			if (bambooServer.getPassword().length() > 0) {
                loger.error("Bamboo login exception: " + e.getMessage());
                connectionErrorMessage = e.getMessage();
            } else {
                throw new ServerPasswordNotProvidedException();
            }
        } catch (RemoteApiException e) {
            loger.error("Bamboo exception: " + e.getMessage());
            connectionErrorMessage = e.getMessage();
        }

        Collection<BambooPlan> plansForServer = null;
        try {
            plansForServer = getPlanList(bambooServer);
        } catch (RemoteApiException e) {
            // can go further, no disabled info will be available
        }

        if (bambooServer.isUseFavourites()) {
            if (plansForServer != null) {
                for (BambooPlan bambooPlan : plansForServer) {
                    if (bambooPlan.isFavourite()) {
                        if (api != null && api.isLoggedIn()) {
                            try {
                                BambooBuild buildInfo = api.getLatestBuildForPlan(bambooPlan.getPlanKey());
                                ((BambooBuildInfo) buildInfo).setServer(bambooServer);
                                ((BambooBuildInfo) buildInfo).setEnabled(bambooPlan.isEnabled());
                                builds.add(buildInfo);
                            } catch (RemoteApiException e) {
                                // go ahead, there are other builds
                            }
                        } else {
                            builds.add(constructBuildErrorInfo(
                                    bambooServer,
                                    bambooPlan.getPlanKey(),
                                    connectionErrorMessage));
                        }
                    }
                }
            }
        } else {
            for (SubscribedPlan plan : bambooServer.getSubscribedPlans()) {
                if (api != null && api.isLoggedIn()) {
                    try {
                        BambooBuild buildInfo = api.getLatestBuildForPlan(plan.getPlanId());
                        ((BambooBuildInfo) buildInfo).setEnabled(true);
                        ((BambooBuildInfo) buildInfo).setServer(bambooServer);
                        if (plansForServer != null) {
                            for (BambooPlan bambooPlan : plansForServer) {
                                if (plan.getPlanId().equals(bambooPlan.getPlanKey())) {
                                    ((BambooBuildInfo) buildInfo).setEnabled(bambooPlan.isEnabled());
                                }
                            }
                        }
                        builds.add(buildInfo);
                    } catch (RemoteApiException e) {
                        // go ahead, there are other builds
                    }
                } else {
                    builds.add(constructBuildErrorInfo(
                            bambooServer, plan.getPlanId(), connectionErrorMessage));
                }
            }
        }


        return builds;
    }

    /**
     * @param bambooServer
     * @param buildKey
     * @param buildNumber
     * @return
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    public BuildDetails getBuildDetails(BambooServerCfg bambooServer, String buildKey, String buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            return api.getBuildResultDetails(buildKey, buildNumber);
        } catch (RemoteApiException e) {
            loger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
     * @param bambooServer
     * @param buildKey
     * @param buildNumber
     * @param buildLabel
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    public void addLabelToBuild(BambooServerCfg bambooServer, String buildKey, String buildNumber, String buildLabel)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            api.addLabelToBuild(buildKey, buildNumber, buildLabel);
        } catch (RemoteApiException e) {
            loger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
     * @param bambooServer
     * @param buildKey
     * @param buildNumber
     * @param buildComment
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    public void addCommentToBuild(BambooServerCfg bambooServer, String buildKey, String buildNumber, String buildComment)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            api.addCommentToBuild(buildKey, buildNumber, buildComment);
        } catch (RemoteApiException e) {
            loger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
     * @param bambooServer
     * @param buildKey
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    public void executeBuild(BambooServerCfg bambooServer, String buildKey)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            api.executeBuild(buildKey);
        } catch (RemoteApiException e) {
            loger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    public byte[] getBuildLogs(BambooServerCfg bambooServer, String buildKey, String buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            return api.getBuildLogs(buildKey, buildNumber);
        } catch (RemoteApiException e) {
            loger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
     * List plans defined on Bamboo server.
     *
     * @param bambooServer Bamboo server information
     * @return list of plans or null on error
     * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
     *          when invoked for Server that has not had the password set yet
     */
    public Collection<String> getFavouritePlans(BambooServerCfg bambooServer)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            return getSession(bambooServer).getFavouriteUserPlans();
        } catch (RemoteApiException e) {
            loger.error("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }


    private BambooBuild constructBuildErrorInfo(BambooServerCfg server, String planId, String message) {
        BambooBuildInfo buildInfo = new BambooBuildInfo();

        buildInfo.setServer(server);
        buildInfo.setServerUrl(server.getUrl());
        buildInfo.setBuildKey(planId);
        buildInfo.setBuildState(BuildStatus.UNKNOWN.toString());
		buildInfo.setMessage(message);
		buildInfo.setPollingTime(new Date());

		return buildInfo;
	}

}
