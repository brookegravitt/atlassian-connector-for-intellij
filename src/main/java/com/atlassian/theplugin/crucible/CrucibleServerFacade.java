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
