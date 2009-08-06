package com.atlassian.connector.intellij.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
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
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class IntelliJCrucibleServerFacade implements CrucibleServerFacade {

	private final CrucibleServerFacade2 facade = CrucibleServerFacadeImpl.getInstance();
	private static IntelliJCrucibleServerFacade instance;

	public static synchronized IntelliJCrucibleServerFacade getInstance() {
		if (instance == null) {
			instance = new IntelliJCrucibleServerFacade();
		}
		return instance;
	}

	private IntelliJCrucibleServerFacade() {

	}

	public Review abandonReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.abandonReview(server.toConnectionCfg(), permId);
	}

	public GeneralComment addGeneralComment(ServerData server, PermId permId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.addGeneralComment(server.toConnectionCfg(), permId, comment);
	}

	public GeneralComment addGeneralCommentReply(ServerData server, PermId id, PermId cId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.addGeneralCommentReply(server.toConnectionCfg(), id, cId, comment);
	}

	public Review addItemsToReview(ServerData server, PermId permId, Collection<UploadItem> items) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.addItemsToReview(server.toConnectionCfg(), permId, items);
	}

	public Review addPatchToReview(ServerData server, PermId permId, String repository, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.addPatchToReview(server.toConnectionCfg(), permId, repository, patch);
	}

	public void addReviewers(ServerData server, PermId permId, Set<String> userName) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.addReviewers(server.toConnectionCfg(), permId, userName);
	}

	public Review addRevisionsToReview(ServerData server, PermId permId, String repository, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.addRevisionsToReview(server.toConnectionCfg(), permId, repository, revisions);
	}

	public VersionedComment addVersionedComment(ServerData server, PermId permId, PermId riId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.addVersionedComment(server.toConnectionCfg(), permId, riId, comment);
	}

	public VersionedComment addVersionedCommentReply(ServerData server, PermId id, PermId cId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.addVersionedCommentReply(server.toConnectionCfg(), id, cId, comment);
	}

	public Review approveReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.approveReview(server.toConnectionCfg(), permId);
	}

	public boolean checkContentUrlAvailable(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.checkContentUrlAvailable(server.toConnectionCfg());
	}

	public Review closeReview(ServerData server, PermId permId, String summary) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.closeReview(server.toConnectionCfg(), permId, summary);
	}

	public void completeReview(ServerData server, PermId permId, boolean complete) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.completeReview(server.toConnectionCfg(), permId, complete);
	}

	public Review createReview(ServerData server, Review review) throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.createReview(server.toConnectionCfg(), review);
	}

	public Review createReviewFromPatch(ServerData server, Review review, String patch) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.createReviewFromPatch(server.toConnectionCfg(), review, patch);
	}

	public Review createReviewFromRevision(ServerData server, Review review, List<String> revisions) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.createReviewFromRevision(server.toConnectionCfg(), review, revisions);
	}

	public Review createReviewFromUpload(ServerData server, Review review, Collection<UploadItem> uploadItems)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.createReviewFromUpload(server.toConnectionCfg(), review, uploadItems);
	}

	public List<Review> getAllReviews(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.getAllReviews(server.toConnectionCfg());
	}

	public void fillDetailsForReview(ReviewAdapter reviewItem) throws RemoteApiException, ServerPasswordNotProvidedException {
		facade.fillDetailsForReview(reviewItem.getServerData().toConnectionCfg(), reviewItem.getReview());
	}

	public String getDisplayName(ServerData server, String username) {
		return facade.getDisplayName(server.toConnectionCfg(), username);
	}

	public byte[] getFileContent(ServerData server, String contentUrl) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getFileContent(server.toConnectionCfg(), contentUrl);
	}

	public Set<CrucibleFileInfo> getFiles(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getFiles(server.toConnectionCfg(), permId);
	}

	public List<GeneralComment> getGeneralComments(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getGeneralComments(server.toConnectionCfg(), permId);
	}

	public List<CustomFieldDef> getMetrics(ServerData server, int version) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getMetrics(server.toConnectionCfg(), version);
	}

	public CrucibleProject getProject(ServerData server, String projectKey) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getProject(server.toConnectionCfg(), projectKey);
	}

	public List<CrucibleProject> getProjects(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.getProjects(server.toConnectionCfg());
	}

	public List<Repository> getRepositories(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.getRepositories(server.toConnectionCfg());
	}

	public SvnRepository getRepository(ServerData server, String repoName) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getRepository(server.toConnectionCfg(), repoName);
	}

	public Review getReview(ServerData server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.getReview(server.toConnectionCfg(), permId);
	}

	public List<Reviewer> getReviewers(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getReviewers(server.toConnectionCfg(), permId);
	}

	public List<Review> getReviewsForCustomFilter(ServerData server, CustomFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getReviewsForCustomFilter(server.toConnectionCfg(), filter);
	}

	public List<Review> getReviewsForFilter(ServerData server, PredefinedFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getReviewsForFilter(server.toConnectionCfg(), filter);
	}

	public List<User> getUsers(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.getUsers(server.toConnectionCfg());
	}

	public List<VersionedComment> getVersionedComments(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.getVersionedComments(server.toConnectionCfg(), permId);
	}

	public List<VersionedComment> getVersionedComments(ServerData server, PermId permId, PermId reviewItemId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.getVersionedComments(server.toConnectionCfg(), permId, reviewItemId);
	}

	public void publishAllCommentsForReview(ServerData server, PermId reviewId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.publishAllCommentsForReview(server.toConnectionCfg(), reviewId);
	}

	public void publishComment(ServerData server, PermId reviewId, PermId commentId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.publishComment(server.toConnectionCfg(), reviewId, commentId);
	}

	public Review recoverReview(ServerData server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.recoverReview(server.toConnectionCfg(), permId);
	}

	public void removeComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.removeComment(server.toConnectionCfg(), id, comment);
	}

	public void removeReviewer(ServerData server, PermId permId, String userName) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.removeReviewer(server.toConnectionCfg(), permId, userName);
	}

	public Review reopenReview(ServerData server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.reopenReview(server.toConnectionCfg(), permId);
	}

	public void setCallback(HttpSessionCallback callback) {
		facade.setCallback(callback);
	}

	public void setReviewers(ServerData server, PermId permId, Collection<String> usernames) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.setReviewers(server.toConnectionCfg(), permId, usernames);
	}

	public Review submitReview(ServerData server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.submitReview(server.toConnectionCfg(), permId);
	}

	public Review summarizeReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return facade.summarizeReview(server.toConnectionCfg(), permId);
	}

	public void updateComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.updateComment(server.toConnectionCfg(), id, comment);
	}

	public ServerType getServerType() {
		return facade.getServerType();
	}

	public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
		facade.testServerConnection(connectionCfg);
	}

    public void markCommentRead(ServerData server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.markCommentRead(server.toConnectionCfg(), reviewId, commentId);
    }

    public void markCommentLeaveUnread(ServerData server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.markCommentLeaveUnread(server.toConnectionCfg(), reviewId, commentId);
    }

    public void markAllCommentsRead(ServerData server, PermId reviewId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.markAllCommentsRead(server.toConnectionCfg(), reviewId);
    }
}
