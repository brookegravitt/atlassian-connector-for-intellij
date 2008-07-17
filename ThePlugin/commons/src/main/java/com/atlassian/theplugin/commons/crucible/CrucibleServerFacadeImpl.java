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

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CrucibleServerFacadeImpl implements CrucibleServerFacade {
    private Map<String, CrucibleSession> sessions = new HashMap<String, CrucibleSession>();
    private static CrucibleServerFacadeImpl instance;
	private CrucibleServerFacadeImpl() {
    }

    public static CrucibleServerFacade getInstance() {
        if (instance == null) {
            instance = new CrucibleServerFacadeImpl();
        }
        return instance;
    }

    public ServerType getServerType() {
        return ServerType.CRUCIBLE_SERVER;
    }

    private synchronized CrucibleSession getSession(Server server) throws RemoteApiException {
		String key = server.getUrlString() + server.getUserName() + server.transientGetPasswordString();
        CrucibleSession session = sessions.get(key);
        if (session == null) {
            session = new CrucibleSessionImpl(server.getUrlString());
            sessions.put(key, session);
        }
        if (!session.isLoggedIn()) {
            session.login(server.getUserName(), server.transientGetPasswordString());
        }
        return session;
    }

    /**
     * @param serverUrl @see
     *                  com.atlassian.theplugin.commons.crucible.remoteapi.soap.CrucibleSessionImpl#constructor(String baseUrl)
     * @param userName
     * @param password
     * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
     *
     */
    public void testServerConnection(String serverUrl, String userName, String password) throws RemoteApiException {
        CrucibleSession session = null;
		session = new CrucibleSessionImpl(serverUrl);
        session.login(userName, password);
        session.logout();
    }

    /**
     * Creates new review in Crucible
     *
     * @param server
     * @param review data for new review to create (some fields have to be set e.g. projectKey)
     * @return created revew date
     * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
     *          in case of createReview error or CrucibleLoginException in case of login error
     */
    public Review createReview(Server server, Review review) throws RemoteApiException {
        CrucibleSession session = getSession(server);
        return session.createReview(review);
    }

    public Review createReviewFromRevision(
            Server server,
            Review review,
            List<String> revisions) throws RemoteApiException {
        CrucibleSession session = getSession(server);
        Review newReview = null;
        if (!revisions.isEmpty()) {
            newReview = session.createReviewFromRevision(review, revisions);
        }
        return newReview;
    }

    public List<CrucibleAction> getAvailableActions(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getAvailableActions(permId);
    }

    public List<Transition> getAvailableTransitions(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getAvailableTransitions(permId);
    }

    public Review addRevisionsToReview(
            Server server,
            PermId permId,
            String repository,
            List<String> revisions) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        Review review = null;
        if (!revisions.isEmpty()) {
            review = session.addRevisionsToReview(permId, repository, revisions);
        }
        return review;
    }

    public Review addPatchToReview(
            Server server,
            PermId permId,
            String repository,
            String patch) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        Review review = session.addPatchToReview(permId, repository, patch);
        return review;
    }


    public void addReviewers(
            Server server,
            PermId permId,
            Set<String> userNames) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.addReviewers(permId, userNames);
    }

    public Review approveReview(
            Server server,
            PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.approveReview(permId);
    }

    public Review summarizeReview(Server server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.summarizeReview(permId);
    }

    public Review abandonReview(Server server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.abandonReview(permId);
    }

    public Review closeReview(Server server, PermId permId, String summary)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.closeReview(permId, summary);
    }

    public Review recoverReview(Server server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.recoverReview(permId);
    }

    public Review reopenReview(Server server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.reopenReview(permId);
    }

    public Review rejectReview(Server server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.rejectReview(permId);
    }

    public void completeReview(Server server, PermId permId, boolean complete)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.completeReview(permId, complete);
    }

    /**
     * Creates new review in Crucible
     *
     * @param server
     * @param review data for new review to create (some fields have to be set e.g. projectKey)
     * @param patch  patch to assign with the review
     * @return created revew date
     * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
     *          in case of createReview error or CrucibleLoginException in case of login error
     */
    public Review createReviewFromPatch(Server server, Review review, String patch)
            throws RemoteApiException {
        CrucibleSession session = getSession(server);
        return session.createReviewFromPatch(review, patch);
    }

    public List<CrucibleFileInfo> getFiles(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getFiles(permId);
    }

    public List<Comment> getComments(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getComments(permId);
    }

    public List<GeneralComment> getGeneralComments(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getGeneralComments(permId);
    }

    public List<VersionedComment> getVersionedComments(Server server, PermId permId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getAllVersionedComments(permId);
    }

    public List<VersionedComment> getVersionedComments(Server server, PermId permId, PermId reviewItemId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getVersionedComments(permId, reviewItemId);
    }

    public List<GeneralComment> getReplies(Server server, PermId permId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getReplies(permId, commentId);
    }

    public GeneralComment addGeneralComment(Server server, PermId permId, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.addGeneralComment(permId, comment);
    }

    public VersionedComment addVersionedComment(Server server, PermId permId, VersionedComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.addVersionedComment(permId, comment);
    }

    public void updateGeneralComment(Server server, PermId id, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.updateGeneralComment(id, comment);
    }

    public void publishComment(Server server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.publishComment(reviewId, commentId);
    }

    public void publishAllCommentsForReview(Server server, PermId reviewId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.publishComment(reviewId, null);
    }

    public GeneralComment addReply(Server server, PermId id, PermId cId, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.addReply(id, cId, comment);
    }

    public void updateReply(Server server, PermId id, PermId cId, PermId rId, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.updateReply(id, cId, rId, comment);
    }

    public void removeGeneralComment(Server server, PermId id, GeneralComment comment)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.removeGeneralComment(id, comment);
    }

    public List<User> getUsers(Server server) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getUsers();
    }

    /**
     * Retrieves list of projects defined on Crucible server
     *
     * @param server
     * @return
     * @throws RemoteApiException
     * @throws ServerPasswordNotProvidedException
     *
     */
    public List<Project> getProjects(Server server) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getProjects();
    }


    /**
     * Retrieves list of repositories defined on Crucible server
     *
     * @param server
     * @return
     * @throws RemoteApiException
     * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
     *
     */
    public List<Repository> getRepositories(Server server)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getRepositories();
    }

    public SvnRepository getRepository(Server server, String repoName)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getRepository(repoName);
    }

    public List<CustomFieldDef> getMetrics(Server server, int version)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getMetrics(version);
    }

    /**
     * @param server server object with Url, Login and Password to connect to
     * @return List of reviews (empty list in case there is no review)
     */
    public List<Review> getAllReviews(Server server) throws RemoteApiException {
        CrucibleSession session = getSession(server);
        return session.getAllReviews(true);
    }

    public List<Review> getReviewsForFilter(Server server, PredefinedFilter filter)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getReviewsForFilter(filter, true);
    }

    public List<Review> getReviewsForCustomFilter(Server server, CustomFilter filter)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getReviewsForCustomFilter(filter, true);
    }

}
