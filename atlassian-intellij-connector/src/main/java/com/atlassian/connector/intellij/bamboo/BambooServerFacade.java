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

package com.atlassian.connector.intellij.bamboo;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;

public interface BambooServerFacade extends ProductServerFacade {
	Collection<BambooProject> getProjectList(BambooServerData bambooServer)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	Collection<BambooPlan> getPlanList(BambooServerData bambooServer)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	Collection<BambooBuildAdapter> getSubscribedPlansResults(BambooServerData bambooServer,
			final Collection<SubscribedPlan> plans,
			boolean isUseFavourities, int timezoneOffset)
			throws ServerPasswordNotProvidedException;

	BuildDetails getBuildDetails(BambooServerData bambooServer, @NotNull String planKey, int buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	void addLabelToBuild(BambooServerData bambooServer, @NotNull String planKey, int buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	void addCommentToBuild(BambooServerData bambooServer, @NotNull String planKey, int buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	void executeBuild(BambooServerData bambooServer, @NotNull String planKey)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	String getBuildLogs(BambooServerData bambooServer, @NotNull String planKey, int buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	void setCallback(HttpSessionCallback callback);

	/**
	 * List build history for provided plan.
	 * <p/>
	 * Returns last X builds on provided plan including information about failed attempt.
	 * X may differ depending on build frequence in selected plan.
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer   Bamboo server information
	 * @param planKey		key of the plan to query
	 * @param timezoneOffset
	 * @return last X builds for selected plan
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 */
	Collection<BambooBuildAdapter> getRecentBuildsForPlans(BambooServerData bambooServer, String planKey,
			final int timezoneOffset)
			throws ServerPasswordNotProvidedException;

	/**
	 * List build history for current user.<p>
	 * <p/>
	 * Returns last builds for selected user including information about failed attempt.<p>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer Bamboo server information
	 * @return last builds for the user (as configred in <code>bambooServer</code>)
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 */
	Collection<BambooBuildAdapter> getRecentBuildsForUser(BambooServerData bambooServer, final int timezoneOffset)
			throws ServerPasswordNotProvidedException;

	BambooBuildAdapter getBuildForPlanAndNumber(BambooServerData bambooServer, @NotNull String planKey,
			final int buildNumber, final int timezoneOffset)
			throws ServerPasswordNotProvidedException, RemoteApiException;

	boolean isBamboo2(BambooServerData serverData);

	boolean isBamboo2M9(final BambooServerData bambooServerData);
}
