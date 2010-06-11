package com.atlassian.connector.intellij.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallback;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.PathAndRevision;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCacheImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ExtendedCrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
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
		private final Map<ConnectionCfg, Map<String, BasicProject>> serverMap =
                new ConcurrentHashMap<ConnectionCfg, Map<String, BasicProject>>();



		public Map<String, BasicProject> getProjects(ConnectionCfg server)
                throws RemoteApiException, ServerPasswordNotProvidedException {
			Map<String, BasicProject> projects = serverMap.get(server);
            if (projects == null) {
                projects = refreshProjectsFromServer(server);
            }
            return projects;
        }

		public void registerProject(ConnectionCfg server, ExtendedCrucibleProject project) {
			Map<String, BasicProject> projectsForServer = serverMap.get(server);
			if (projectsForServer == null) {
				projectsForServer = MiscUtil.buildHashMap();
			}
			projectsForServer.put(project.getKey(), project);
		}

        @NotNull
		private Map<String, BasicProject> refreshProjectsFromServer(ConnectionCfg server)
                throws RemoteApiException, ServerPasswordNotProvidedException {
			final List<BasicProject> projects = facade.getProjects(server);

			Map<String, BasicProject> map = new HashMap<String, BasicProject>(projects.size() + 1, 1);

            for (BasicProject project : projects) {
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
        this(new CrucibleServerFacadeImpl(LoggerImpl.getInstance(), new CrucibleUserCacheImpl(), 
        		new IntelliJHttpSessionCallback()));
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
		return toReviewAdapter(changeReviewState(server, permId, CrucibleAction.ABANDON), server);
    }

	public Review changeReviewState(ServerData server, PermId permId, CrucibleAction action) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		final BasicReview review = facade.getSession(server).changeReviewState(permId, action);
		return facade.getReview(server, review.getPermId());
	}

    private ReviewAdapter toReviewAdapter(@NotNull Review review, ServerData serverData)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return new ReviewAdapter(review, serverData, getProject(serverData, review.getProjectKey()));
    }

	public Comment addGeneralComment(ServerData server, Review review, Comment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.addGeneralComment(server, review, comment);
    }

	public Comment addReply(ServerData server, Comment reply)
            throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.addReply(server, reply);
    }

    public ReviewAdapter addItemsToReview(ServerData server, PermId permId, Collection<UploadItem> items)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.addItemsToReview(server, permId, items), server);
    }

    public ReviewAdapter addPatchToReview(ServerData server, PermId permId, String repository, String patch)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.addPatchToReview(server, permId, repository, patch), server);
    }

    public void addReviewers(ServerData server, PermId permId, Set<String> userName) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.addReviewers(server, permId, userName);
    }

    public ReviewAdapter addRevisionsToReview(ServerData server, PermId permId, String repository, List<String> revisions)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.addRevisionsToReview(server, permId, repository, revisions),
                server);
    }

    public ReviewAdapter addFileVersionsToReview(ServerData server, PermId permId, String repoName,
                                                 List<PathAndRevision> pathsAndRevisions)
            throws RemoteApiException, ServerPasswordNotProvidedException {

        return toReviewAdapter(facade.addFileRevisionsToReview(server,
                permId, repoName, pathsAndRevisions), server);
    }

	public VersionedComment addVersionedComment(ServerData server, Review review, PermId riId, VersionedComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.addVersionedComment(server, review, riId, comment);
    }


    public ReviewAdapter approveReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
		return toReviewAdapter(changeReviewState(server, permId, CrucibleAction.APPROVE), server);
    }

    public boolean checkContentUrlAvailable(ServerData server)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.checkContentUrlAvailable(server);
    }

    public ReviewAdapter closeReview(ServerData server, PermId permId, String summary) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.closeReview(server, permId, summary), server);
    }

    public void completeReview(ServerData server, PermId permId, boolean complete) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.completeReview(server, permId, complete);
    }

    public ReviewAdapter createReview(ServerData server, Review review)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.createReview(server, review), server);
    }

    public ReviewAdapter createReviewFromPatch(ServerData server, Review review, String patch) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.createReviewFromPatch(server, review, patch), server);
    }

    public ReviewAdapter createReviewFromRevision(ServerData server, Review review, List<String> revisions)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.createReviewFromRevision(server, review, revisions), server);
    }

    public ReviewAdapter createReviewFromUpload(ServerData server, Review review, Collection<UploadItem> uploadItems)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.createReviewFromUpload(server, review, uploadItems), server);
    }

    public String getDisplayName(@NotNull ServerData server, @NotNull String username) {
        return facade.getDisplayName(server, username);
    }

    public byte[] getFileContent(@NotNull ServerData server, String contentUrl) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getFileContent(server, contentUrl);
    }

//    public Set<CrucibleFileInfo> getFiles(ServerData server, PermId permId) throws RemoteApiException,
//            ServerPasswordNotProvidedException {
//        return facade.getFiles(server, permId);
//    }

	// public List<Comment> getGeneralComments(ServerData server, PermId permId) throws RemoteApiException,
	// ServerPasswordNotProvidedException {
	// return facade.getGeneralComments(server, permId);
	// }

    public List<CustomFieldDef> getMetrics(ServerData server, int version) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getMetrics(server, version);
    }

    @Nullable
	public ExtendedCrucibleProject getProject(@NotNull ServerData server, @NotNull String projectKey)
			throws RemoteApiException,
            ServerPasswordNotProvidedException {
		final Map<String, BasicProject> projectsByKey = projectCache.getProjects(server);
		final BasicProject crucibleProject = projectsByKey.get(projectKey);
        
        if (crucibleProject instanceof ExtendedCrucibleProject) {
			return (ExtendedCrucibleProject) crucibleProject;
		} else {
			final ExtendedCrucibleProject project = facade.getSession(server).getProject(projectKey);
            if (project != null) {
			    projectCache.registerProject(server, project);
            }
			return project;
		}
    }

	private List<ReviewAdapter> toReviewAdapterList(Collection<BasicReview> reviews, ServerData server)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        final ArrayList<ReviewAdapter> res = MiscUtil.buildArrayList(reviews.size());
		for (BasicReview basicReview : reviews) {
			Review review = facade.getReview(server, basicReview.getPermId());
            res.add(toReviewAdapter(review, server));
        }
        return res;
    }

    /**
     * Does caching, as IntelliJ Connector does not have its own meta-data cache
     */
	public List<BasicProject> getProjects(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
        return MiscUtil.buildArrayList(projectCache.getProjects(server).values());
    }

    public List<Repository> getRepositories(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.getRepositories(server);
    }

    public Repository getRepository(ServerData server, String repoName) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getRepository(server, repoName);
    }

    public ReviewAdapter getReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        return toReviewAdapter(facade.getReview(server, permId), server);
    }

    public List<Reviewer> getReviewers(ServerData server, PermId permId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return facade.getReviewers(server, permId);
    }

	// @todo performance is crappy here: N + 1 remote calls. This method should return List<BasicReviewAdapter>
    public List<ReviewAdapter> getReviewsForCustomFilter(ServerData server, CustomFilter filter) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return toReviewAdapterList(facade.getReviewsForCustomFilter(server, filter), server);
    }

	// @todo performance is crappy here: N + 1 remote calls. This method should return List<BasicReviewAdapter>
    public List<ReviewAdapter> getReviewsForFilter(ServerData server, PredefinedFilter filter) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        return toReviewAdapterList(facade.getReviewsForFilter(server, filter), server);
    }

    public List<User> getUsers(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
        return facade.getUsers(server);
    }

//	public List<VersionedComment> getVersionedComments(ServerData server, Review review) throws RemoteApiException,
//            ServerPasswordNotProvidedException {
//		return facade.getVersionedComments(server, review);
//    }

	public List<VersionedComment> getVersionedComments(ServerData server, Review review, CrucibleFileInfo reviewItem)
            throws RemoteApiException, ServerPasswordNotProvidedException {
		return facade.getVersionedComments(server, review, reviewItem);
    }

    public void publishAllCommentsForReview(ServerData server, PermId reviewId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.publishAllCommentsForReview(server, reviewId);
    }

    public void publishComment(ServerData server, PermId reviewId, PermId commentId) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.publishComment(server, reviewId, commentId);
    }

    public ReviewAdapter recoverReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
		return toReviewAdapter(changeReviewState(server, permId, CrucibleAction.RECOVER), server);
    }

    public void removeComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.removeComment(server, id, comment);
    }

    public void removeReviewer(ServerData server, PermId permId, String userName) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.removeReviewer(server, permId, userName);
    }

    public ReviewAdapter reopenReview(ServerData server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
		return toReviewAdapter(changeReviewState(server, permId, CrucibleAction.REOPEN), server);
    }

    public void setReviewers(@NotNull ServerData server, @NotNull PermId permId, @NotNull Collection<String> usernames)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.setReviewers(server, permId, usernames);
    }

    public void updateComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
            ServerPasswordNotProvidedException {
        facade.updateComment(server, id, comment);
    }

    public ServerType getServerType() {
        return facade.getServerType();
    }


    public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
        facade.testServerConnection(connectionCfg);
    }

    public void markCommentRead(ServerData server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.markCommentRead(server, reviewId, commentId);
    }

    public void markCommentLeaveUnread(ServerData server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.markCommentLeaveUnread(server, reviewId, commentId);
    }

    public void markAllCommentsRead(ServerData server, PermId reviewId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        facade.markAllCommentsRead(server, reviewId);
    }

    @Nullable
    public List<User> getAllowedReviewers(ServerData server, String projectKey) throws RemoteApiException,
            ServerPasswordNotProvidedException {

		final ExtendedCrucibleProject project = getProject(server, projectKey);
        if (project == null) {
            return null;
        }
        final Collection<String> allowedReviewersStr = project.getAllowedReviewers();
        // this by the way will populate the cache so that subsequent getUser() have a chance to work
        final List<User> allUsers = facade.getUsers(server);
        if (allowedReviewersStr != null) {

            List<User> allowedReviewers = new ArrayList<User>();
            for (String userName : allowedReviewersStr) {
                final User user = facade.getUser(server, userName);
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
