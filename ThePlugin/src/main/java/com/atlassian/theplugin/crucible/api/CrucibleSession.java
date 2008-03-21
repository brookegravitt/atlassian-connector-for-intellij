package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.remoteapi.RemoteApiException;
import com.atlassian.theplugin.remoteapi.RemoteApiLoginException;

import java.util.List;


public interface CrucibleSession {
	void login(String userName, String password) throws RemoteApiLoginException;

	void logout();

	ReviewData createReview(ReviewData reviewData) throws RemoteApiException;

	ReviewData createReviewFromPatch(ReviewData reviewData, String patch) throws RemoteApiException;

	List<ReviewData> getReviewsInStates(List<State> arg1) throws RemoteApiException;

	List<ReviewData> getAllReviews() throws RemoteApiException;

	List<String> getReviewers(PermId arg1) throws RemoteApiException;

	List<ProjectData> getProjects() throws RemoteApiException;

	List<RepositoryData> getRepositories() throws RemoteApiException;

	boolean isLoggedIn();	
}
