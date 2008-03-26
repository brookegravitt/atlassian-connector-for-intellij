package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.remoteapi.RemoteApiException;
import com.atlassian.theplugin.remoteapi.ProductServerFacade;

import java.util.Collection;

public interface BambooServerFacade extends ProductServerFacade {
	Collection<BambooProject> getProjectList(Server bambooServer) throws ServerPasswordNotProvidedException, RemoteApiException;

    Collection<BambooPlan> getPlanList(Server bambooServer) throws ServerPasswordNotProvidedException, RemoteApiException;

    Collection<BambooBuild> getSubscribedPlansResults(Server bambooServer) throws ServerPasswordNotProvidedException;

	BuildDetails getBuildDetails(Server bambooServer, String buildKey, String buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	void addLabelToBuild(Server bambooServer, String buildKey, String buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	void addCommentToBuild(Server bambooServer, String buildKey, String buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	void executeBuild(Server bambooServer, String buildKey)
			throws ServerPasswordNotProvidedException, RemoteApiException;
}
