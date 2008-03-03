package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.Server;

import java.util.Collection;

public interface BambooServerFacade {
    void testServerConnection(String url, String userName, String password) throws BambooLoginException;

    Collection<BambooProject> getProjectList(Server bambooServer) throws ServerPasswordNotProvidedException;

    Collection<BambooPlan> getPlanList(Server bambooServer) throws ServerPasswordNotProvidedException;

    Collection<BambooBuild> getSubscribedPlansResults(Server bambooServer) throws ServerPasswordNotProvidedException;

	BuildDetails getBuildDetails(Server bambooServer, String buildKey, String buildNumber)
			throws ServerPasswordNotProvidedException;
}
