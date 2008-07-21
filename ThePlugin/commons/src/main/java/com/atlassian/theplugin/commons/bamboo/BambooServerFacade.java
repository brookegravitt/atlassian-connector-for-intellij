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

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;

import java.util.Collection;
import java.util.List;

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

    byte[] getBuildLogs(Server server, String buildKey, String buildNumber) throws ServerPasswordNotProvidedException, RemoteApiException;
}
