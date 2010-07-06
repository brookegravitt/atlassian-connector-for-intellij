/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.intellij.bamboo;

import com.atlassian.connector.commons.api.BambooServerFacade2;
import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.BuildIssue;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.bamboo.api.BambooSession;
import com.atlassian.theplugin.commons.cfg.ConfigurationListener;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Provides simple wrapper around IDE-independent {@link BambooServerFacade} for use by IntelliJ Connector (it's mostly about
 * retaining BambooServerData object in returned values)
 * 
 * @author wseliga
 * 
 */
public class IntelliJBambooServerFacade implements BambooServerFacade {

	private static IntelliJBambooServerFacade instance;
	private final BambooServerFacade2 facade;
    private final IntelliJHttpSessionCallbackImpl callback;
    private ConfigurationListenerAdapter configurationListener;

    private IntelliJBambooServerFacade(Logger logger) {
        callback = new IntelliJHttpSessionCallbackImpl();
        facade = new BambooServerFacadeImpl(logger, callback);
        configurationListener =  new ConfigurationListenerAdapter() {
                @Override
                public void serverRemoved(ServerData serverData) {
                    callback.disposeClient(serverData.toConnectionCfg());
                }

            @Override
            public void serverConnectionDataChanged(ServerId serverId) {
                callback.disposeClient(serverId);
            }

            @Override
            public void serverDisabled(ServerId serverId) {
                callback.disposeClient(serverId);                
            }
        };

	}

	public static synchronized IntelliJBambooServerFacade getInstance(Logger logger) {
		if (instance == null) {
			instance = new IntelliJBambooServerFacade(logger);
            
		}

		return instance;
	}

	public void addCommentToBuild(BambooServerData bambooServer, @NotNull String planKey, int buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		facade.addCommentToBuild(bambooServer, planKey, buildNumber, buildComment);
	}

	public void addLabelToBuild(BambooServerData bambooServer, @NotNull String planKey, int buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		facade.addLabelToBuild(bambooServer, planKey, buildNumber, buildComment);
	}

	public void executeBuild(BambooServerData bambooServer, @NotNull String planKey) throws ServerPasswordNotProvidedException,
			RemoteApiException {
		facade.executeBuild(bambooServer, planKey);
	}

	public BuildDetails getBuildDetails(BambooServerData bambooServer, @NotNull String planKey, int buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		return facade.getBuildDetails(bambooServer, planKey, buildNumber);
	}

	public BambooBuildAdapter getBuildForPlanAndNumber(BambooServerData bambooServer, @NotNull String planKey, int buildNumber,
			int timezoneOffset) throws ServerPasswordNotProvidedException, RemoteApiException {
		return new BambooBuildAdapter(facade.getBuildForPlanAndNumber(bambooServer, planKey, buildNumber,
				timezoneOffset), bambooServer);
	}

	public String getBuildLogs(BambooServerData bambooServer, @NotNull String planKey, int buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		return facade.getBuildLogs(bambooServer, planKey, buildNumber);
	}

	public Collection<BambooPlan> getPlanList(BambooServerData bambooServer) throws ServerPasswordNotProvidedException,
			RemoteApiException {
		return facade.getPlanList(bambooServer);
	}

	public Collection<BambooProject> getProjectList(BambooServerData bambooServer) throws ServerPasswordNotProvidedException,
			RemoteApiException {
		return facade.getProjectList(bambooServer);
	}

	public Collection<BambooBuildAdapter> getRecentBuildsForPlans(BambooServerData bambooServer, String planKey,
			int timezoneOffset) throws ServerPasswordNotProvidedException {
		final Collection<BambooBuild> builds =
				facade.getRecentBuildsForPlans(bambooServer, planKey, timezoneOffset);
		return convertToBambooBuildAdapters(bambooServer, builds);
	}

	private static ArrayList<BambooBuildAdapter> convertToBambooBuildAdapters(BambooServerData bambooServer,
			final Collection<BambooBuild> builds) {
		final ArrayList<BambooBuildAdapter> res = MiscUtil.buildArrayList(builds.size());
		for (BambooBuild bambooBuild : builds) {
			res.add(new BambooBuildAdapter(bambooBuild, bambooServer));
		}
		return res;
	}

	public Collection<BambooBuildAdapter> getRecentBuildsForUser(BambooServerData bambooServer, int timezoneOffset)
			throws ServerPasswordNotProvidedException {
		return convertToBambooBuildAdapters(bambooServer, facade.getRecentBuildsForUser(bambooServer,
				timezoneOffset));
	}

	public Collection<BambooBuildAdapter> getSubscribedPlansResults(BambooServerData bambooServer,
			Collection<SubscribedPlan> plans, boolean isUseFavourities, int timezoneOffset)
			throws ServerPasswordNotProvidedException, RemoteApiException {

		try {
			return convertToBambooBuildAdapters(bambooServer,
					facade.getSubscribedPlansResults(bambooServer,
							plans, isUseFavourities, timezoneOffset));
		} catch (RemoteApiLoginException e) {
			Collection<BambooBuildAdapter> res = MiscUtil.buildArrayList(plans.size());
			for (SubscribedPlan plan : plans) {
				res.add(new BambooBuildAdapter(constructBuildErrorInfo(bambooServer, plan.getKey(),
						null, e.getMessage() == null ? "" : e.getMessage(), e), bambooServer));

			}
			return res;
		}
	}

    public Collection<BuildIssue> getIssuesForBuild(BambooServerData bambooServer,
                                                    @NotNull String planKey, int buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        return facade.getIssuesForBuild(bambooServer, planKey, buildNumber);
    }

    private BambooBuild constructBuildErrorInfo(ConnectionCfg server, @NotNull String planKey, String planName,
			String message, Throwable exception) {
		return new BambooBuildInfo.Builder(planKey, null, server, planName, null, BuildStatus.UNKNOWN).errorMessage(
				message, exception).pollingTime(new Date()).build();
	}


	public boolean isBamboo2(final BambooServerData serverData) {
		BambooSession session;
		try {
			session = facade.getSession(serverData);
			if (session != null && session.getBamboBuildNumber() > 0) {
				return true;
			}

		} catch (RemoteApiException e) {
			// not important == false
		}
		return false;
	}


    public ServerType getServerType() {
		return facade.getServerType();
	}

    public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
        facade.testServerConnection(connectionCfg);
    }


    public void disposeClient(ConnectionCfg server) {
            callback.disposeClient(server);
    }

    public ConfigurationListener getConfigurationListener() {
           return configurationListener;
    }

}
