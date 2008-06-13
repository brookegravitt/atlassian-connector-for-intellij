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

package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.crucible.api.model.*;

import java.util.List;
import java.util.Set;

public interface CrucibleServerFacade extends ProductServerFacade {
	Review createReview(Server server, Review review)
			throws RemoteApiException, ServerPasswordNotProvidedException;

    Review createReviewFromRevision(Server server, Review review, List<String> revisions)
            throws RemoteApiException;

    List<CrucibleAction> getAvailableActions(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    Review addRevisionsToReview(Server server, PermId permId, String repository, List<String> revisions)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    Review addPatchToReview(Server server, PermId permId, String repository, String patch)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    void addReviewers(Server server, PermId permId, Set<String> userName)
		    throws RemoteApiException, ServerPasswordNotProvidedException;    

    Review approveReview(Server server, PermId permId)
		    throws RemoteApiException, ServerPasswordNotProvidedException;

    Review summarizeReview(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    Review abandonReview(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    Review closeReview(Server server, PermId permId, String summary)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    Review recoverReview(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    Review reopenReview(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    Review rejectReview(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException;
    
    void completeReview(Server server, PermId permId, boolean complete)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    List<ReviewInfo> getAllReviews(Server server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<ReviewInfo> getActiveReviewsForUser(Server server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

    List<ReviewInfo> getReviewsForFilter(Server server, PredefinedFilter filter)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    List<ReviewInfo> getReviewsForCustomFilter(Server server, CustomFilter filter)
            throws RemoteApiException, ServerPasswordNotProvidedException;    

    Review createReviewFromPatch(Server server, Review review, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<ReviewItem> getReviewItems(Server server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<GeneralComment> getComments(Server server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

    List<GeneralComment> getGeneralComments(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    List<VersionedComment> getVersionedComments(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    List<VersionedComment> getVersionedComments(Server server, PermId permId, PermId reviewItemId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    List<GeneralComment> getReplies(Server server, PermId permId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    GeneralComment addGeneralComment(Server server, PermId permId, GeneralComment comment) 
            throws RemoteApiException, ServerPasswordNotProvidedException;

    VersionedComment addVersionedComment(Server server, PermId permId, VersionedComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException;
    
    void updateGeneralComment(Server server, PermId id, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    void publishComment(Server server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    void publishAllCommentsForReview(Server server, PermId reviewId)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    GeneralComment addReply(Server server, PermId id, PermId cId, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException;    

    void updateReply(Server server, PermId id, PermId cId, PermId rId, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    void removeGeneralComment(Server server, PermId id, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException;
    
    List<User> getUsers(Server server)
            throws RemoteApiException, ServerPasswordNotProvidedException;

    List<Project> getProjects(Server server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Repository> getRepositories(Server server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	SvnRepository getRepository(Server server, String repoName)
			throws RemoteApiException, ServerPasswordNotProvidedException;

    List<CustomFieldDef> getMetrics(Server server, int version)
			throws RemoteApiException, ServerPasswordNotProvidedException;

}
