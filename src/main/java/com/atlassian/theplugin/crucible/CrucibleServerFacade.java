package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.crucible.api.CrucibleSession;
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
	public void testServerConnection(String serverUrl, String userName, String password) throws CrucibleException;

	public ReviewData createReview(Server server, ReviewData review) throws CrucibleException;

	/**
	 *
	 * @param server object with Url and other server data
	 * @return list of all reviews
	 */
	public List<Object> getAllReviews(Server server) throws CrucibleLoginException;

	void setCrucibleSession(CrucibleSession crucibleSession);
}
