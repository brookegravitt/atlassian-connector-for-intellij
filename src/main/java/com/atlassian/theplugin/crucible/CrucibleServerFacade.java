package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.crucible.api.ProjectData;
import com.atlassian.theplugin.crucible.api.RepositoryData;
import com.atlassian.theplugin.crucible.api.ReviewData;
import com.atlassian.theplugin.rest.RestException;

import java.util.List;

public interface CrucibleServerFacade {
	void testServerConnection(String serverUrl, String userName, String password) throws RestException;

	ReviewData createReview(Server server, ReviewData review)
			throws RestException, ServerPasswordNotProvidedException;

	List<ReviewDataInfo> getAllReviews(Server server)
			throws RestException, ServerPasswordNotProvidedException;

	List<ReviewDataInfo> getActiveReviewsForUser(Server server)
			throws RestException, ServerPasswordNotProvidedException;

	ReviewData createReviewFromPatch(Server server, ReviewData reviewData, String patch)
			throws RestException, ServerPasswordNotProvidedException;

	List<ProjectData> getProjects(Server server)
			throws RestException, ServerPasswordNotProvidedException;

	List<RepositoryData> getRepositories(Server server) 
			throws RestException, ServerPasswordNotProvidedException;

}
