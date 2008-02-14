package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:22:10
 * To change this template use File | Settings | File Templates.
 */
public interface CrucibleServerFacade {
	void testServerConnection(String serverUrl, String userName, String password) throws CrucibleException;

	ReviewData createReview(Server server, ReviewData review)
			throws CrucibleException, ServerPasswordNotProvidedException;

	List<ReviewData> getAllReviews(Server server)
			throws CrucibleException, ServerPasswordNotProvidedException;

	List<RemoteReview> getActiveReviewsForUser(Server server)
			throws CrucibleException, ServerPasswordNotProvidedException;

	ReviewData createReviewFromPatch(ServerBean server, ReviewData reviewData, String patch)
			throws CrucibleException, ServerPasswordNotProvidedException;
}
