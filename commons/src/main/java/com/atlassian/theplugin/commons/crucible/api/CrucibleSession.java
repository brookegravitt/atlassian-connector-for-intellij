/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.crucible.api;

import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;

import java.util.List;
import java.util.Set;


public interface CrucibleSession {
	void login(String userName, String password) throws RemoteApiLoginException;

	void logout();

	ReviewData createReview(ReviewData reviewData) throws RemoteApiException;

	ReviewData createReviewFromPatch(ReviewData reviewData, String patch) throws RemoteApiException;

    ReviewData createReviewFromRevision(ReviewData reviewData, List<String> revisions) throws RemoteApiException;

    ReviewData addRevisionsToReview(PermId permId, String repository, List<String> revisions) throws RemoteApiException;

    ReviewData addPatchToReview(PermId permId, String repository, String patch) throws RemoteApiException;    

    void addReviewers(PermId permId, Set<String> userNames) throws RemoteApiException;

    ReviewData approveReview(PermId permId) throws RemoteApiException;

    List<ReviewData> getReviewsInStates(List<State> arg1) throws RemoteApiException;

	List<ReviewData> getAllReviews() throws RemoteApiException;

    List<ReviewData> getReviewsForFilter(PredefinedFilter filter) throws RemoteApiException;

    List<ReviewData> getReviewsForCustomFilter(CustomFilter filter) throws RemoteApiException;

    List<UserData> getReviewers(PermId arg1) throws RemoteApiException;

    List<UserData> getUsers() throws RemoteApiException;

    List<ProjectData> getProjects() throws RemoteApiException;

	List<RepositoryData> getRepositories() throws RemoteApiException;

	SvnRepositoryData getRepository(String repoName) throws RemoteApiException;	

	List<ReviewItemData> getReviewItems(PermId id) throws RemoteApiException;

	List<GeneralComment> getGeneralComments(PermId id) throws RemoteApiException;

	List<VersionedComment> getVersionedComments(PermId id) throws RemoteApiException;	

	List<GeneralComment> getComments(PermId id) throws RemoteApiException;

	boolean isLoggedIn();
}
