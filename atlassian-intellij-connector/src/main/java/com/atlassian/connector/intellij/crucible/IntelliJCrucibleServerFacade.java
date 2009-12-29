package com.atlassian.connector.intellij.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallback;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.PathAndRevision;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCacheImpl;
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
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.IdeaHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class IntelliJCrucibleServerFacade extends ConfigurationListenerAdapter implements CrucibleServerFacade {


    public class CrucibleProjectCacheImpl extends ConfigurationListenerAdapter {
        private final Map<ConnectionCfg, Map<String, CrucibleProject>> serverMap
                = new ConcurrentHashMap<ConnectionCfg, Map<String, CrucibleProject>>();


        public Map<String, CrucibleProject> getProjects(ConnectionCfg server)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            Map<String, CrucibleProject> projects = serverMap.get(server);
            if (projects == null) {
                projects = refreshProjectsFromServer(server);
            }
            return projects;
        }

        @NotNull
        private Map<String, CrucibleProject> refreshProjectsFromServer(ConnectionCfg server)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            final List<CrucibleProject> projects = facade.getProjects(server);

            Map<String, CrucibleProject> map = new HashMap<String, CrucibleProject>(projects.size() + 1, 1);

            for (CrucibleProject project : projects) {
                map.put(project.getKey(), project);
            }
            serverMap.put(server, map);
            return map;
        }

        public synchronized void clearCache() {
            serverMap.clear();
        }


        private void removeServerId(ServerId serverId) {
            Set<ConnectionCfg> connections = serverMap.keySet();
            for (ConnectionCfg con : connections) {
                if (con.getId().equals(serverId.toString())) {
                    serverMap.remove(con);
                    return;
                }
            }
        }
    }

    private final CrucibleServerFacadeImpl facade;
    private static IntelliJCrucibleServerFacade instance;
    private final CrucibleProjectCacheImpl projectCache = new CrucibleProjectCacheImpl();

    public static synchronized IntelliJCrucibleServerFacade getInstance() {
        if (instance == null) {
            instance = new IntelliJCrucibleServerFacade();
        }
        return instance;
    }

    private IntelliJCrucibleServerFacade() {
        this(new CrucibleServerFacadeImpl(new CrucibleUserCacheImpl(), new IntelliJHttpSessionCallback()));
    }

    IntelliJCrucibleServerFacade(CrucibleServerFacadeImpl facade) {
        this.facade = facade;
    }

    @Override
    public void serverConnectionDataChanged(ServerId serverId) {
        if (projectCache != null) {
            projectCache.removeServerId(serverId);
        }
    }

    public void clearCache() {
        if (projectCache != null) {
            projectCache.clearCache();
        }
    }

    public ReviewAdapter abandonReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.abandonReview(server.toHttpConnectionCfg(), permId), server);
    }

    private ReviewAdapter toReviewAdapter(@NotNull Review review, ServerData serverData)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return new ReviewAdapter(review, serverData, getProject(serverData, review.getProjectKey()));
    }

    public GeneralComment addGeneralComment(ServerData server, PermId permId, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.addGeneralComment(server.toHttpConnectionCfg(), permId, comment);
    }

    public GeneralComment addGeneralCommentReply(ServerData server, PermId id, PermId cId, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.addGeneralCommentReply(server.toHttpConnectionCfg(), id, cId, comment);
    }

    public ReviewAdapter addItemsToReview(ServerData server, PermId permId, Collection<UploadItem> items)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.addItemsToReview(server.toHttpConnectionCfg(), permId, items), server);
    }

    public ReviewAdapter addPatchToReview(ServerData server, PermId permId, String repository, String patch)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.addPatchToReview(server.toHttpConnectionCfg(), permId, repository, patch), server);
    }

    public void addReviewers(ServerData server, PermId permId, Set<String> userName) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.addReviewers(server.toHttpConnectionCfg(), permId, userName);
    }

    public ReviewAdapter addRevisionsToReview(ServerData server, PermId permId, String repository, List<String> revisions)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.addRevisionsToReview(server.toHttpConnectionCfg(), permId, repository, revisions),
                server);
    }

    public ReviewAdapter addFileVersionsToReview(ServerData server, PermId permId, String repoName,
                                                 List<PathAndRevision> pathsAndRevisions)
            throws RemoteApiException, ServerPasswordNotProvidedException {

        return toReviewAdapter(facade.addFileRevisionsToReview(server.toHttpConnectionCfg(),
                permId, repoName, pathsAndRevisions), server);
    }

    public VersionedComment addVersionedComment(ServerData server, PermId permId, PermId riId, VersionedComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.addVersionedComment(server.toHttpConnectionCfg(), permId, riId, comment);
    }

    public VersionedComment addVersionedCommentReply(ServerData server, PermId id, PermId cId, VersionedComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.addVersionedCommentReply(server.toHttpConnectionCfg(), id, cId, comment);
    }

    public ReviewAdapter approveReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.approveReview(server.toHttpConnectionCfg(), permId), server);
    }

    public boolean checkContentUrlAvailable(ServerData server)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.checkContentUrlAvailable(server.toHttpConnectionCfg());
    }

    public ReviewAdapter closeReview(ServerData server, PermId permId, String summary) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.closeReview(server.toHttpConnectionCfg(), permId, summary), server);
    }

    public void completeReview(ServerData server, PermId permId, boolean complete) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.completeReview(server.toHttpConnectionCfg(), permId, complete);
    }

    public ReviewAdapter createReview(ServerData server, Review review)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.createReview(server.toHttpConnectionCfg(), review), server);
    }

    public ReviewAdapter createReviewFromPatch(ServerData server, Review review, String patch) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.createReviewFromPatch(server.toHttpConnectionCfg(), review, patch), server);
    }

    public ReviewAdapter createReviewFromRevision(ServerData server, Review review, List<String> revisions)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.createReviewFromRevision(server.toHttpConnectionCfg(), review, revisions), server);
    }

    public ReviewAdapter createReviewFromUpload(ServerData server, Review review, Collection<UploadItem> uploadItems)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.createReviewFromUpload(server.toHttpConnectionCfg(), review, uploadItems), server);
    }

    public List<ReviewAdapter> getAllReviews(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapterList(facade.getAllReviews(server.toHttpConnectionCfg()), server);
    }

    public void fillDetailsForReview(ReviewAdapter reviewItem) throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.fillDetailsForReview(reviewItem.getServerData().toHttpConnectionCfg(), reviewItem.getReview());
    }

    public String getDisplayName(@NotNull ServerData server, @NotNull String username) {
        return facade.getDisplayName(server.toHttpConnectionCfg(), username);
    }

    public byte[] getFileContent(@NotNull ServerData server, String contentUrl) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getFileContent(server.toHttpConnectionCfg(), contentUrl);
    }

    public Set<CrucibleFileInfo> getFiles(ServerData server, PermId permId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getFiles(server.toHttpConnectionCfg(), permId);
    }

    public List<GeneralComment> getGeneralComments(ServerData server, PermId permId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getGeneralComments(server.toHttpConnectionCfg(), permId);
    }

    public List<CustomFieldDef> getMetrics(ServerData server, int version) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getMetrics(server.toHttpConnectionCfg(), version);
    }

    @Nullable
    public CrucibleProject getProject(@NotNull ServerData server, @NotNull String projectKey) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        final Map<String, CrucibleProject> projectsByKey = projectCache.getProjects(server.toHttpConnectionCfg());
        final CrucibleProject crucibleProject = projectsByKey.get(projectKey);
        if (crucibleProject == null) {
            return projectCache.refreshProjectsFromServer(server.toHttpConnectionCfg()).get(projectKey);
        }
        return crucibleProject;
    }

    private List<ReviewAdapter> toReviewAdapterList(Collection<Review> reviews, ServerData server)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        final ArrayList<ReviewAdapter> res = MiscUtil.buildArrayList(reviews.size());
        for (Review review : reviews) {
            res.add(toReviewAdapter(review, server));
        }
        return res;
    }

    /**
     * Does caching, as IntelliJ Connector does not have its own meta-data cache
     */
    public List<CrucibleProject> getProjects(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
        return MiscUtil.buildArrayList(projectCache.getProjects(server.toHttpConnectionCfg()).values());
    }

    public List<Repository> getRepositories(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.getRepositories(server.toHttpConnectionCfg());
    }

    public SvnRepository getRepository(ServerData server, String repoName) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getRepository(server.toHttpConnectionCfg(), repoName);
    }

    public ReviewAdapter getReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.getReview(server.toHttpConnectionCfg(), permId), server);
    }

    public List<Reviewer> getReviewers(ServerData server, PermId permId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getReviewers(server.toHttpConnectionCfg(), permId);
    }

    public List<ReviewAdapter> getReviewsForCustomFilter(ServerData server, CustomFilter filter) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return toReviewAdapterList(facade.getReviewsForCustomFilter(server.toHttpConnectionCfg(), filter), server);
    }

    public List<ReviewAdapter> getReviewsForFilter(ServerData server, PredefinedFilter filter) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return toReviewAdapterList(facade.getReviewsForFilter(server.toHttpConnectionCfg(), filter), server);
    }

    public List<User> getUsers(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.getUsers(server.toHttpConnectionCfg());
    }

    public List<VersionedComment> getVersionedComments(ServerData server, PermId permId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getVersionedComments(server.toHttpConnectionCfg(), permId);
    }

    public List<VersionedComment> getVersionedComments(ServerData server, PermId permId, PermId reviewItemId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.getVersionedComments(server.toHttpConnectionCfg(), permId, reviewItemId);
    }

    public void publishAllCommentsForReview(ServerData server, PermId reviewId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.publishAllCommentsForReview(server.toHttpConnectionCfg(), reviewId);
    }

    public void publishComment(ServerData server, PermId reviewId, PermId commentId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.publishComment(server.toHttpConnectionCfg(), reviewId, commentId);
    }

    public ReviewAdapter recoverReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.recoverReview(server.toHttpConnectionCfg(), permId), server);
    }

    public void removeComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.removeComment(server.toHttpConnectionCfg(), id, comment);
    }

    public void removeReviewer(ServerData server, PermId permId, String userName) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.removeReviewer(server.toHttpConnectionCfg(), permId, userName);
    }

    public ReviewAdapter reopenReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.reopenReview(server.toHttpConnectionCfg(), permId), server);
    }

    public void setReviewers(@NotNull ServerData server, @NotNull PermId permId, @NotNull Collection<String> usernames)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.setReviewers(server.toHttpConnectionCfg(), permId, usernames);
    }

    public ReviewAdapter submitReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.submitReview(server.toHttpConnectionCfg(), permId), server);
    }

    public ReviewAdapter summarizeReview(ServerData server, PermId permId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.summarizeReview(server.toHttpConnectionCfg(), permId), server);
    }

    public void updateComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.updateComment(server.toHttpConnectionCfg(), id, comment);
    }

    public ServerType getServerType() {
        return facade.getServerType();
    }

    public void testServerConnection(HttpConnectionCfg httpConnectionCfg) throws RemoteApiException {
        facade.testServerConnection(httpConnectionCfg);
    }

    public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
        facade.testServerConnection(connectionCfg);
    }

    public void markCommentRead(ServerData server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.markCommentRead(server.toHttpConnectionCfg(), reviewId, commentId);
    }

    public void markCommentLeaveUnread(ServerData server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.markCommentLeaveUnread(server.toHttpConnectionCfg(), reviewId, commentId);
    }

    public void markAllCommentsRead(ServerData server, PermId reviewId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.markAllCommentsRead(server.toHttpConnectionCfg(), reviewId);
    }

    @Nullable
    public List<User> getAllowedReviewers(ServerData server, String projectKey) throws RemoteApiException,
            ServerPasswordNotProvidedException {

        final CrucibleProject project = getProject(server, projectKey);
        if (project == null) {
            return null;
        }
        final Collection<String> allowedReviewersStr = project.getAllowedReviewers();
        // this by the way will populate the cache so that subsequent getUser() have a chance to work
        final List<User> allUsers = facade.getUsers(server.toHttpConnectionCfg());
        if (allowedReviewersStr != null) {

            List<User> allowedReviewers = new ArrayList<User>();
            for (String userName : allowedReviewersStr) {
                final User user = facade.getUser(server.toHttpConnectionCfg(), userName);
                if (user != null) {
                    allowedReviewers.add(user);
                }
            }
            return allowedReviewers;
        }
        // otherwise we assume all users are allowed (old Crucible 1.6.x does not serve this data)
        return allUsers;
    }
}
