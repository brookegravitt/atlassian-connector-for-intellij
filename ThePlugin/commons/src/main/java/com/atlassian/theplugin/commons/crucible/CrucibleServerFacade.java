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

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.List;
import java.util.Set;

public interface CrucibleServerFacade extends ProductServerFacade {
//	CrucibleVersionInfo getServerVersion(CrucibleServerCfg server)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review createReview(CrucibleServerCfg server, Review review)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review createReviewFromRevision(CrucibleServerCfg server, Review review, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	List<Action> getAvailableActions(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

//	List<Action> getAvailableTransitions(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review addRevisionsToReview(CrucibleServerCfg server, PermId permId, String repository, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review addPatchToReview(CrucibleServerCfg server, PermId permId, String repository, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	CrucibleFileInfo addItemToReview(CrucibleServerCfg server, Review review, NewReviewItem newItem)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Reviewer> getReviewers(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	void addReviewers(CrucibleServerCfg server, PermId permId, Set<String> userName)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	void removeReviewer(CrucibleServerCfg server, PermId permId, String userName)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review approveReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review submitReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review summarizeReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review abandonReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review closeReview(CrucibleServerCfg server, PermId permId, String summary)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review recoverReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review reopenReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	Review rejectReview(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	void completeReview(CrucibleServerCfg server, PermId permId, boolean complete)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Review> getAllReviews(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Review> getReviewsForFilter(CrucibleServerCfg server, PredefinedFilter filter)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Review> getReviewsForCustomFilter(CrucibleServerCfg server, CustomFilter filter)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review getReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	List<Review> getAllReviewsForFile(CrucibleServerCfg server, String repoName, String path)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	Review createReviewFromPatch(CrucibleServerCfg server, Review review, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<CrucibleFileInfo> getFiles(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	List<Comment> getComments(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<GeneralComment> getGeneralComments(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<VersionedComment> getVersionedComments(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<VersionedComment> getVersionedComments(CrucibleServerCfg server, PermId permId, PermId reviewItemId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	List<GeneralComment> getReplies(CrucibleServerCfg server, PermId permId, PermId commentId)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	GeneralComment addGeneralComment(CrucibleServerCfg server, PermId permId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	VersionedComment addVersionedComment(CrucibleServerCfg server, PermId permId, PermId riId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	void updateComment(CrucibleServerCfg server, PermId id, Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	void publishComment(CrucibleServerCfg server, PermId reviewId, PermId commentId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	void publishAllCommentsForReview(CrucibleServerCfg server, PermId reviewId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	GeneralComment addGeneralCommentReply(CrucibleServerCfg server, PermId id, PermId cId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	VersionedComment addVersionedCommentReply(CrucibleServerCfg server, PermId id, PermId cId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	void updateReply(CrucibleServerCfg server, PermId id, PermId cId, PermId rId, GeneralComment comment)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	void removeComment(CrucibleServerCfg server, PermId id, Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<User> getUsers(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Project> getProjects(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Repository> getRepositories(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	SvnRepository getRepository(CrucibleServerCfg server, String repoName)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<CustomFieldDef> getMetrics(CrucibleServerCfg server, int version)
			throws RemoteApiException, ServerPasswordNotProvidedException;

}
