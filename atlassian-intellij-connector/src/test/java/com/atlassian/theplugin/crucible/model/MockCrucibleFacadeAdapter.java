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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public class MockCrucibleFacadeAdapter implements CrucibleServerFacade {
	public ReviewAdapter createReview(ServerData server, Review review)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public String getDisplayName(@NotNull final ServerData server, @NotNull final String username) {
		return null;
	}

	@Nullable
	public CrucibleProject getProject(@NotNull final ServerData server, @NotNull final String projectKey)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public boolean checkContentUrlAvailable(final ServerData server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return false;
	}

	public ReviewAdapter createReviewFromUpload(final ServerData server, final Review review,
			final Collection<UploadItem> uploadItems)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public byte[] getFileContent(@NotNull final ServerData server, final String contentUrl)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return new byte[0];
	}


	public ReviewAdapter addItemsToReview(final ServerData server, final PermId permId, final Collection<UploadItem> items)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void fillDetailsForReview(final ReviewAdapter reviewItem)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public ReviewAdapter createReviewFromRevision(ServerData server, Review review, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter addRevisionsToReview(ServerData server, PermId permId,
			String repository, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter addPatchToReview(ServerData server, PermId permId, String repository, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<Reviewer> getReviewers(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void addReviewers(ServerData server, PermId permId, Set<String> userName)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public void removeReviewer(ServerData server, PermId permId, String userName)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public void setReviewers(@NotNull final ServerData server, @NotNull final PermId permId,
			@NotNull final Collection<String> userName) throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public ReviewAdapter approveReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter submitReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter summarizeReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter abandonReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter closeReview(ServerData server, PermId permId, String summary)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter recoverReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter reopenReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void completeReview(ServerData server, PermId permId, boolean complete)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public List<ReviewAdapter> getAllReviews(ServerData server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<ReviewAdapter> getReviewsForFilter(ServerData server, PredefinedFilter filter)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<ReviewAdapter> getReviewsForCustomFilter(ServerData server, CustomFilter filter)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter getReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public ReviewAdapter createReviewFromPatch(ServerData server, Review review, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public Set<CrucibleFileInfo> getFiles(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<GeneralComment> getGeneralComments(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<VersionedComment> getVersionedComments(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<VersionedComment> getVersionedComments(ServerData server, PermId permId, PermId reviewItemId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public GeneralComment addGeneralComment(ServerData server, PermId permId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public VersionedComment addVersionedComment(ServerData server, PermId permId,
			PermId riId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void updateComment(ServerData server, PermId id, Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public void publishComment(ServerData server, PermId reviewId, PermId commentId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public void publishAllCommentsForReview(ServerData server, PermId reviewId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public GeneralComment addGeneralCommentReply(ServerData server, PermId id,
			PermId cId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public VersionedComment addVersionedCommentReply(ServerData server, PermId id,
			PermId cId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void removeComment(ServerData server, PermId id, Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
	}

	public List<User> getUsers(ServerData server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<CrucibleProject> getProjects(ServerData server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<Repository> getRepositories(ServerData server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public SvnRepository getRepository(ServerData server, String repoName)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public List<CustomFieldDef> getMetrics(ServerData server, int version)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return null;
	}

	public void testServerConnection(String url, String userName, String password)
			throws RemoteApiException {
	}

	public void setCallback(final HttpSessionCallback callback) {
	}

	public void testServerConnection(final ConnectionCfg serverCfg) throws RemoteApiException {
	}

	public ServerType getServerType() {
		return null;
	}

    public void markCommentRead(ServerData server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
    }

    public void markCommentLeaveUnread(ServerData server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
    }

    public void markAllCommentsRead(ServerData server, PermId reviewId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
    }

    public List<User> getAllowedReviewers(ServerData server, String projectKey) 
			throws RemoteApiException, ServerPasswordNotProvidedException {
        return null;  
    }
}
