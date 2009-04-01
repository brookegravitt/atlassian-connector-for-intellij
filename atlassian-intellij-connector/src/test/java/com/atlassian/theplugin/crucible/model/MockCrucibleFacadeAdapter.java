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
package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public class MockCrucibleFacadeAdapter implements CrucibleServerFacade {
	public Review createReview(CrucibleServerCfg server, Review review)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public String getDisplayName(@NotNull final CrucibleServerCfg server, @NotNull final String username) {
		return null;
	}

	@Nullable
	public CrucibleProject getProject(@NotNull final CrucibleServerCfg server, @NotNull final String projectKey)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public boolean checkContentUrlAvailable(final CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return false;
	}

	public Review createReviewFromUpload(final CrucibleServerCfg server, final Review review,
			final Collection<UploadItem> uploadItems)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public byte[] getFileContent(@NotNull final CrucibleServerCfg server, final String contentUrl)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return new byte[0];
	}


	public Review addItemsToReview(final CrucibleServerCfg server, final PermId permId, final Collection<UploadItem> items)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review createReviewFromRevision(CrucibleServerCfg server, Review review, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review addRevisionsToReview(CrucibleServerCfg server, PermId permId,
			String repository, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review addPatchToReview(CrucibleServerCfg server, PermId permId, String repository, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<Reviewer> getReviewers(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void addReviewers(CrucibleServerCfg server, PermId permId, Set<String> userName)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public void removeReviewer(CrucibleServerCfg server, PermId permId, String userName)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public void setReviewers(final CrucibleServerCfg server, final PermId permId, final Collection<String> userName)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public Review approveReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review submitReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review summarizeReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review abandonReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review closeReview(CrucibleServerCfg server, PermId permId, String summary)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review recoverReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review reopenReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void completeReview(CrucibleServerCfg server, PermId permId, boolean complete)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public List<Review> getAllReviews(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<Review> getReviewsForFilter(CrucibleServerCfg server, PredefinedFilter filter)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<Review> getReviewsForCustomFilter(CrucibleServerCfg server, CustomFilter filter)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review getReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Review createReviewFromPatch(CrucibleServerCfg server, Review review, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Set<CrucibleFileInfo> getFiles(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<GeneralComment> getGeneralComments(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<VersionedComment> getVersionedComments(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<VersionedComment> getVersionedComments(CrucibleServerCfg server, PermId permId, PermId reviewItemId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public GeneralComment addGeneralComment(CrucibleServerCfg server, PermId permId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public VersionedComment addVersionedComment(CrucibleServerCfg server, PermId permId,
			PermId riId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void updateComment(CrucibleServerCfg server, PermId id, Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public void publishComment(CrucibleServerCfg server, PermId reviewId, PermId commentId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public void publishAllCommentsForReview(CrucibleServerCfg server, PermId reviewId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public GeneralComment addGeneralCommentReply(CrucibleServerCfg server, PermId id,
			PermId cId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public VersionedComment addVersionedCommentReply(CrucibleServerCfg server, PermId id,
			PermId cId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void removeComment(CrucibleServerCfg server, PermId id, Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public List<User> getUsers(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<CrucibleProject> getProjects(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<Repository> getRepositories(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public SvnRepository getRepository(CrucibleServerCfg server, String repoName)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<CustomFieldDef> getMetrics(CrucibleServerCfg server, int version)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void testServerConnection(String url, String userName, String password)
			throws RemoteApiException {
	}

	public void setCallback(final HttpSessionCallback callback) {
	}

	public void testServerConnection(final ServerCfg serverCfg) throws RemoteApiException {
	}

	public ServerType getServerType() {
		return null;
	}
}
