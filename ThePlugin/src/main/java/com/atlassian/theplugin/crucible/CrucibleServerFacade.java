package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.crucible.api.ProjectData;
import com.atlassian.theplugin.crucible.api.RepositoryData;
import com.atlassian.theplugin.crucible.api.ReviewData;

import java.util.List;

public interface CrucibleServerFacade {
	void testServerConnection(String serverUrl, String userName, String password) throws CrucibleException;

	ReviewData createReview(Server server, ReviewData review)
			throws CrucibleException, ServerPasswordNotProvidedException;

	List<ReviewDataInfo> getAllReviews(Server server)
			throws CrucibleException, ServerPasswordNotProvidedException;

	List<ReviewDataInfo> getActiveReviewsForUser(Server server)
			throws CrucibleException, ServerPasswordNotProvidedException;

	ReviewData createReviewFromPatch(Server server, ReviewData reviewData, String patch)
			throws CrucibleException, ServerPasswordNotProvidedException;

	List<ProjectData> getProjects(Server server)
			throws CrucibleException, ServerPasswordNotProvidedException;

	List<RepositoryData> getRepositories(Server server) 
			throws CrucibleException, ServerPasswordNotProvidedException;

}
