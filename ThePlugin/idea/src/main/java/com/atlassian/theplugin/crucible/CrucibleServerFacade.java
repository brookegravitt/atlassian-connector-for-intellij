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

package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.crucible.api.ProjectData;
import com.atlassian.theplugin.crucible.api.RepositoryData;
import com.atlassian.theplugin.crucible.api.ReviewData;
import com.atlassian.theplugin.remoteapi.RemoteApiException;

import java.util.List;

public interface CrucibleServerFacade extends ProductServerFacade {
	ReviewData createReview(Server server, ReviewData review)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<ReviewDataInfo> getAllReviews(Server server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<ReviewDataInfo> getActiveReviewsForUser(Server server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	ReviewData createReviewFromPatch(Server server, ReviewData reviewData, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<ProjectData> getProjects(Server server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<RepositoryData> getRepositories(Server server) 
			throws RemoteApiException, ServerPasswordNotProvidedException;

}
