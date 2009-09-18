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

package com.atlassian.connector.intellij.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CrucibleServerFacade extends ProductServerFacade {
//	CrucibleVersionInfo getServerVersion(CrucibleServerCfg server)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	ReviewAdapter createReview(ServerData server, Review review) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter createReviewFromRevision(ServerData server, Review review, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	ReviewAdapter addRevisionsToReview(ServerData server, PermId permId, String repository, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	ReviewAdapter addPatchToReview(ServerData server, PermId permId, String repository, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	CrucibleFileInfo addItemToReview(CrucibleServerCfg server, Review review, NewReviewItem newItem)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Reviewer> getReviewers(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	void addReviewers(ServerData server, PermId permId, Set<String> userName) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	void removeReviewer(ServerData server, PermId permId, String userName) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	/**
	 * Convenience method for setting reviewers for a review. Please keep in mind that it involves at least 3 remote
	 * calls to Crucible server: getReview(), addReviewers() and N times removeReviewer(). This method is not atomic, so
	 * it may fail and leave reviewers in partially updated state After this method is complete, reviewers for selected
	 * review will be equal to this as given by <code>usernames</code>. Reviewers which are in <code>usernames</code>
	 * and are also present in the review itself are left intact - i.e. the method does gurantee to leave them intact
	 * even if some problems occur during execution.
	 *
	 * @param server	Crucible server to connect to
	 * @param permId	id of review
	 * @param usernames usernames of reviewers
	 * @throws RemoteApiException in case of some connection problems or malformed responses
	 * @throws ServerPasswordNotProvidedException
	 *                            when password was not provided
	 */
	void setReviewers(@NotNull ServerData server, @NotNull PermId permId, @NotNull Collection<String> usernames)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	ReviewAdapter approveReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter submitReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter summarizeReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter abandonReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter closeReview(ServerData server, PermId permId, String summary) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter recoverReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter reopenReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

//	Review rejectReview(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	void completeReview(ServerData server, PermId permId, boolean complete) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	List<ReviewAdapter> getAllReviews(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException;

	List<ReviewAdapter> getReviewsForFilter(ServerData server, PredefinedFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	List<ReviewAdapter> getReviewsForCustomFilter(ServerData server, CustomFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter getReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

//	List<Review> getAllReviewsForFile(CrucibleServerCfg server, String repoName, String path)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	ReviewAdapter createReviewFromPatch(ServerData server, Review review, String patch) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	Set<CrucibleFileInfo> getFiles(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

//	List<Comment> getComments(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	List<GeneralComment> getGeneralComments(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	List<VersionedComment> getVersionedComments(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	List<VersionedComment> getVersionedComments(ServerData server, PermId permId, PermId reviewItemId)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	List<GeneralComment> getReplies(CrucibleServerCfg server, PermId permId, PermId commentId)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	GeneralComment addGeneralComment(ServerData server, PermId permId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	VersionedComment addVersionedComment(ServerData server, PermId permId, PermId riId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	void updateComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	void publishComment(ServerData server, PermId reviewId, PermId commentId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	void publishAllCommentsForReview(ServerData server, PermId reviewId) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	GeneralComment addGeneralCommentReply(ServerData server, PermId id, PermId cId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	VersionedComment addVersionedCommentReply(ServerData server, PermId id, PermId cId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException;

//	void updateReply(CrucibleServerCfg server, PermId id, PermId cId, PermId rId, GeneralComment comment)
//			throws RemoteApiException, ServerPasswordNotProvidedException;

	void removeComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	List<User> getUsers(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException;

	List<CrucibleProject> getProjects(ServerData server) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	List<Repository> getRepositories(ServerData server) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	SvnRepository getRepository(ServerData server, String repoName) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	List<CustomFieldDef> getMetrics(ServerData server, int version) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	@Nullable
	String getDisplayName(@NotNull final ServerData server, @NotNull String username);

	@Nullable
	CrucibleProject getProject(@NotNull final ServerData server, @NotNull final String projectKey)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	boolean checkContentUrlAvailable(ServerData server) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter createReviewFromUpload(ServerData server, Review review, Collection<UploadItem> uploadItems)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	byte[] getFileContent(@NotNull ServerData server, final String contentUrl) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	ReviewAdapter addItemsToReview(ServerData server, PermId permId, Collection<UploadItem> items)
			throws RemoteApiException, ServerPasswordNotProvidedException;

	void fillDetailsForReview(final ReviewAdapter reviewItem) throws RemoteApiException, ServerPasswordNotProvidedException;

    void markCommentRead(ServerData server, PermId reviewId, PermId commentId)  throws RemoteApiException,
			ServerPasswordNotProvidedException;

    void markCommentLeaveUnread(ServerData server, PermId reviewId, PermId commentId)  throws RemoteApiException,
			ServerPasswordNotProvidedException;

    void markAllCommentsRead(ServerData server, PermId reviewId)  throws RemoteApiException,
			ServerPasswordNotProvidedException;

    List<User> getAllowedReviewers(ServerData server, String projectKey) throws RemoteApiException,
            ServerPasswordNotProvidedException;
}