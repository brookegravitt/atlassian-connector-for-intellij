package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.rest.RestException;
import com.atlassian.theplugin.rest.RestLoginException;

import java.util.Collection;

public interface BambooServerFacade {
    void testServerConnection(String url, String userName, String password) throws RestLoginException;

    Collection<BambooProject> getProjectList(Server bambooServer) throws ServerPasswordNotProvidedException, RestException;

    Collection<BambooPlan> getPlanList(Server bambooServer) throws ServerPasswordNotProvidedException, RestException;

    Collection<BambooBuild> getSubscribedPlansResults(Server bambooServer) throws ServerPasswordNotProvidedException;

	BuildDetails getBuildDetails(Server bambooServer, String buildKey, String buildNumber)
			throws ServerPasswordNotProvidedException, RestException;

	void addLabelToBuild(Server bambooServer, String buildKey, String buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RestException;

	void addCommentToBuild(Server bambooServer, String buildKey, String buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RestException;

	void executeBuild(Server bambooServer, String buildKey)
			throws ServerPasswordNotProvidedException, RestException;
}
