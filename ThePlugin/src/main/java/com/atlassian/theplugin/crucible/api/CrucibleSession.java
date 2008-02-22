package com.atlassian.theplugin.crucible.api;

import java.util.List;


public interface CrucibleSession {
	void login(String userName, String password) throws CrucibleLoginException;

	void logout();

	ReviewData createReview(ReviewData reviewData) throws CrucibleException;

	ReviewData createReviewFromPatch(ReviewData reviewData, String patch) throws CrucibleException;

	List<ReviewData> getReviewsInStates(List<State> arg1) throws CrucibleException;

	List<ReviewData> getAllReviews() throws CrucibleException;

	List<String> getReviewers(PermId arg1) throws CrucibleException;

	boolean isLoggedIn();	
}
