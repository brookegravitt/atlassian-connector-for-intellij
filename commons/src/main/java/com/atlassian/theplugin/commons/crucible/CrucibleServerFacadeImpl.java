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
import com.atlassian.theplugin.commons.crucible.api.*;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;


import java.util.*;

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
     *          com.atlassian.theplugin.commons.crucible.remoteapi.soap.CrucibleSessionImpl#constructor(String baseUrl)
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
	 * @param reviewData data for new review to create (some fields have to be set e.g. projectKey)
	 * @return created revew date
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
     *          in case of createReview error or CrucibleLoginException in case of login error
	 */
	public ReviewData createReview(Server server, ReviewData reviewData) throws RemoteApiException {
		CrucibleSession session = getSession(server);
		return session.createReview(reviewData);
	}

    public void addReviewer(Server server, PermId permId, String userName) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.addReviewer(permId, userName);
    }

    public ReviewData approveReview(Server server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.approveReview(permId);
    }

    /**
	 * Creates new review in Crucible
	 *
	 * @param server
	 * @param reviewData data for new review to create (some fields have to be set e.g. projectKey)
	 * @param patch	  patch to assign with the review
	 * @return created revew date
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
     *          in case of createReview error or CrucibleLoginException in case of login error
	 */
	public ReviewData createReviewFromPatch(Server server, ReviewData reviewData, String patch)
			throws RemoteApiException {
		CrucibleSession session = getSession(server);
		return session.createReviewFromPatch(reviewData, patch);
	}
                                                                       
	public List<ReviewItemData> getReviewItems(Server server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewItems(permId);
	}

	public List<GeneralComment> getComments(Server server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getComments(permId);
	}

    public List<UserData> getUsers(Server server) throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        return session.getUsers();
    }

    /**
	 * Retrieves list of projects defined on Crucible server
	 *
	 * @param server
	 * @return
	 * @throws CrucibleException
	 * @throws ServerPasswordNotProvidedException
	 *
	 */
	public List<ProjectData> getProjects(Server server) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getProjects();
	}


	/**
	 * Retrieves list of repositories defined on Crucible server
	 *
	 * @param server
	 * @return
	 * @throws CrucibleException
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *
	 */
	public List<RepositoryData> getRepositories(Server server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepositories();
	}

	public SvnRepositoryData getRepository(Server server, String repoName)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepository(repoName);
	}


	/**
	 * @param server server object with Url, Login and Password to connect to
	 * @return List of reviews (empty list in case there is no review)
	 */
	public List<ReviewDataInfo> getAllReviews(Server server) throws RemoteApiException {
		CrucibleSession session = getSession(server);

		List<ReviewData> res = session.getAllReviews();
		List<ReviewDataInfo> result = new ArrayList<ReviewDataInfo>(res.size());
		for (ReviewData review : res) {
			List<UserData> reviewers = session.getReviewers(review.getPermaId());
			result.add(new ReviewDataInfoImpl(review, reviewers, server));
		}
		return result;
	}

	public List<ReviewDataInfo> getActiveReviewsForUser(Server server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = null;

		try {
			session = getSession(server);
			List<State> states = new ArrayList<State>();
			states.add(State.REVIEW);

			List<ReviewData> reviews = session.getReviewsInStates(states);
			List<ReviewDataInfo> result = new ArrayList<ReviewDataInfo>(reviews.size());

			for (ReviewData reviewData : reviews) {
				List<UserData> reviewers = session.getReviewers(reviewData.getPermaId());

                for (UserData reviewer : reviewers) {
                    if (reviewer.getUserName().equals(server.getUserName())) {
						result.add(new ReviewDataInfoImpl(reviewData, reviewers, server));
					}
				}
			}
			return result;
		} catch (RemoteApiLoginFailedException e) {
			if (!server.getIsConfigInitialized()) {
                throw new ServerPasswordNotProvidedException();
			} /* else {
// @todo do something with logger
//				PluginUtil.getLogger().error("Crucible login exception: " + e.getMessage());
			}
			  */
		} catch (RemoteApiException e) {
// @todo do something with logger
//            PluginUtil.getLogger().error("Crucible exception: " + e.getMessage());
		}
		return Collections.EMPTY_LIST;
	}

    public List<ReviewDataInfo> getReviewsForFilter(Server server, PredefinedFilter filter)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);

        List<ReviewData> reviews = session.getReviewsForFilter(filter);
        List<ReviewDataInfo> result = new ArrayList<ReviewDataInfo>(reviews.size());

        for (ReviewData reviewData : reviews) {
            result.add(new ReviewDataInfoImpl(reviewData, null, server));
        }

        return result;
    }

    public List<ReviewDataInfo> getReviewsForCustomFilter(Server server, CustomFilter filter)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);

        List<ReviewData> reviews = session.getReviewsForCustomFilter(filter);
        List<ReviewDataInfo> result = new ArrayList<ReviewDataInfo>(reviews.size());

        for (ReviewData reviewData : reviews) {
            result.add(new ReviewDataInfoImpl(reviewData, null, server));
        }
        return result;        
    }

}
