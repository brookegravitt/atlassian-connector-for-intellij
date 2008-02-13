package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.review.State;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId;

import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-07
 * Time: 10:52:25
 * To change this template use File | Settings | File Templates.
 */
public interface CrucibleSession {
	void login(String userName, String password) throws CrucibleLoginException;

	void logout();

	String getAuthToken();

	public List<ReviewData> getReviewsInStates(List<State> arg1);

    public List<ReviewData> getAllReviews();

    List<String> getReviewers(PermId arg1);
}
