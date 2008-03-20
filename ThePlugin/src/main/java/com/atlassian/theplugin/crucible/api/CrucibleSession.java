package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.rest.RestException;
import com.atlassian.theplugin.rest.RestLoginException;

import java.util.List;


public interface CrucibleSession {
	void login(String userName, String password) throws RestLoginException;

	void logout();

	ReviewData createReview(ReviewData reviewData) throws RestException;

	ReviewData createReviewFromPatch(ReviewData reviewData, String patch) throws RestException;

	List<ReviewData> getReviewsInStates(List<State> arg1) throws RestException;

	List<ReviewData> getAllReviews() throws RestException;

	List<String> getReviewers(PermId arg1) throws RestException;

	List<ProjectData> getProjects() throws RestException;

	List<RepositoryData> getRepositories() throws RestException;

	boolean isLoggedIn();	
}
