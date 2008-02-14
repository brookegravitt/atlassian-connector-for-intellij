package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.State;

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

	ReviewData createReview(ReviewData reviewData) throws CrucibleException;

	ReviewData createReviewFromPatch(ReviewData reviewData, String patch) throws CrucibleException;

	List<ReviewData> getReviewsInStates(List<State> arg1) throws CrucibleException;

	List<ReviewData> getAllReviews() throws CrucibleException;

	List<String> getReviewers(PermId arg1) throws CrucibleException;
}
