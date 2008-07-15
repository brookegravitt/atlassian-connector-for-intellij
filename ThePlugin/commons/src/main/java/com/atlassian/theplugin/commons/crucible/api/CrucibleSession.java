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
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

import java.util.List;
import java.util.Set;


public interface CrucibleSession {
	void login(String userName, String password) throws RemoteApiLoginException;

	void logout();

	Review createReview(Review review) throws RemoteApiException;

	Review createReviewFromPatch(Review review, String patch) throws RemoteApiException;

    Review createReviewFromRevision(Review review, List<String> revisions) throws RemoteApiException;

    List<CrucibleAction> getAvailableActions(PermId permId) throws RemoteApiException;

    List<Transition> getAvailableTransitions(PermId permId) throws RemoteApiException;    

    Review addRevisionsToReview(PermId permId, String repository, List<String> revisions) throws RemoteApiException;

    Review addPatchToReview(PermId permId, String repository, String patch) throws RemoteApiException;

    void addReviewers(PermId permId, Set<String> userNames) throws RemoteApiException;

    Review approveReview(PermId permId) throws RemoteApiException;

    Review abandonReview(PermId permId) throws RemoteApiException;

    Review closeReview(PermId permId, String summary) throws RemoteApiException;

    Review recoverReview(PermId permId) throws RemoteApiException;

    Review reopenReview(PermId permId) throws RemoteApiException;

    Review rejectReview(PermId permId) throws RemoteApiException;

    Review summarizeReview(PermId permId) throws RemoteApiException;

    void completeReview(PermId permId, boolean complete) throws RemoteApiException;

    List<CrucibleChangeSet> getReviewsInStates(List<State> arg1, boolean details) throws RemoteApiException;

	List<CrucibleChangeSet> getAllReviews(boolean details) throws RemoteApiException;

    List<CrucibleChangeSet> getReviewsForFilter(PredefinedFilter filter, boolean details) throws RemoteApiException;

    List<CrucibleChangeSet> getReviewsForCustomFilter(CustomFilter filter, boolean details) throws RemoteApiException;

    List<Reviewer> getReviewers(PermId arg1) throws RemoteApiException;

    List<User> getUsers() throws RemoteApiException;

    List<Project> getProjects() throws RemoteApiException;

	List<Repository> getRepositories() throws RemoteApiException;

	SvnRepository getRepository(String repoName) throws RemoteApiException;

	List<CrucibleFileInfo> getFiles(PermId id) throws RemoteApiException;

	List<GeneralComment> getComments(PermId id) throws RemoteApiException;

    List<GeneralComment> getGeneralComments(PermId id) throws RemoteApiException;

    List<VersionedComment> getAllVersionedComments(PermId id) throws RemoteApiException;      

    List<VersionedComment> getVersionedComments(PermId id, PermId reviewItemId) throws RemoteApiException;

    List<GeneralComment> getReplies(PermId id, PermId commentId) throws RemoteApiException;

    GeneralComment addGeneralComment(PermId id, GeneralComment comment) throws RemoteApiException;

    VersionedComment addVersionedComment(PermId id, VersionedComment comment) throws RemoteApiException;

    void removeGeneralComment(PermId id, GeneralComment comment) throws RemoteApiException;

    void updateGeneralComment(PermId id, GeneralComment comment) throws RemoteApiException;

    void publishComment(PermId reviewId, PermId commentId) throws RemoteApiException;    

    GeneralComment addReply(PermId id, PermId cId,  GeneralComment comment) throws RemoteApiException;

    void updateReply(PermId id, PermId cId,  PermId rId, GeneralComment comment) throws RemoteApiException;    

    List<CustomFieldDef> getMetrics(int version) throws RemoteApiException;    

    boolean isLoggedIn();
}
