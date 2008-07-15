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

package com.atlassian.theplugin.commons.crucible.api.rest;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewDataImpl;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.remoteapi.*;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.thirdparty.base64.Base64;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfoImpl;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Communication stub for Crucible REST API.
 */
public class CrucibleSessionImpl extends AbstractHttpSession implements CrucibleSession {
    private static final String AUTH_SERVICE = "/rest-service/auth-v1";
    private static final String REVIEW_SERVICE = "/rest-service/reviews-v1";
    private static final String PROJECTS_SERVICE = "/rest-service/projects-v1";
    private static final String REPOSITORIES_SERVICE = "/rest-service/repositories-v1";
    private static final String USER_SERVICE = "/rest-service/users-v1";

    private static final String LOGIN = "/login";
    private static final String REVIEWS_IN_STATES = "?state=";
    private static final String FILTERED_REVIEWS = "/filter";
    private static final String DETAIL_REVIEW_INFO = "/details";
    private static final String ACTIONS = "/action";
    private static final String TRANSITIONS = "/transition";
    private static final String REVIEWERS = "/reviewers";
    private static final String REVIEW_ITEMS = "/reviewitems";
    private static final String METRICS = "/metrics";    

    private static final String COMMENTS = "/comments";
    private static final String GENERAL_COMMENTS = "/comments/general";
    private static final String VERSIONED_COMMENTS = "/comments/versioned";
    private static final String REPLIES = "/replies";

    private static final String APPROVE_ACTION = "action:approveReview";
    private static final String SUMMARIZE_ACTION = "action:summarizeReview";
    private static final String ABANDON_ACTION = "action:abandonReview";
    private static final String CLOSE_ACTION = "action:closeReview";
    private static final String RECOVER_ACTION = "action:recoverReview";
    private static final String REOPEN_ACTION = "action:reopenReview";
    private static final String REJECT_ACTION = "action:rejectReview";
    private static final String TRANSITION_ACTION = "/transition?action=";

    private static final String PUBLISH_COMMENTS = "/publish";
    private static final String COMPLETE_ACTION = "/complete";
    private static final String UNCOMPLETE_ACTION = "/uncomplete";

    private static final String ADD_CHANGESET = "/addChangeset";
    private static final String ADD_PATCH = "/addPatch";

    private String authToken = null;
	private Server server;


	/**
     * Public constructor for CrucibleSessionImpl.
     *
     * @param server
     */
    public CrucibleSessionImpl(Server server) throws RemoteApiMalformedUrlException {
        super(server.getUrlString());
		this.server = server;
	}

	public Server getServer() {
		return server;
	}

	public void login(String username, String aPassword) throws RemoteApiLoginException {
        if (!isLoggedIn()) {
            String loginUrl;
            try {
                if (username == null || aPassword == null) {
                    throw new RemoteApiLoginException("Corrupted configuration. Username or aPassword null");
                }
                loginUrl = baseUrl + AUTH_SERVICE + LOGIN + "?userName=" + URLEncoder.encode(username, "UTF-8")
                        + "&password=" + URLEncoder.encode(aPassword, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                ///CLOVER:OFF
                throw new RuntimeException("URLEncoding problem: " + e.getMessage());
                ///CLOVER:ON
            }

            try {
                Document doc = retrieveGetResponse(loginUrl);
                String exception = getExceptionMessages(doc);
                if (null != exception) {
                    throw new RemoteApiLoginFailedException(exception);
                }
                XPath xpath = XPath.newInstance("/loginResult/token");
                List elements = xpath.selectNodes(doc);
                if (elements == null) {
                    throw new RemoteApiLoginException("Server did not return any authentication token");
                }
                if (elements.size() != 1) {
                    throw new RemoteApiLoginException("Server did returned excess authentication tokens ("
                            + elements.size() + ")");
                }
                this.authToken = ((Element) elements.get(0)).getText();
                this.userName = username;
                this.password = aPassword;
            } catch (MalformedURLException e) {
                throw new RemoteApiLoginException("Malformed server URL: " + baseUrl, e);
            } catch (UnknownHostException e) {
                throw new RemoteApiLoginException("Unknown host: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new RemoteApiLoginException(e.getMessage(), e);
            } catch (JDOMException e) {
                throw new RemoteApiLoginException("Server returned malformed response", e);
            } catch (RemoteApiSessionExpiredException e) {
                // Crucible does not return this exception
            } catch (IllegalArgumentException e) {
                throw new RemoteApiLoginException("Malformed server URL: " + baseUrl, e);
            }
        }
    }

    public void logout() {
        if (authToken != null) {
            authToken = null;
            userName = null;
            password = null;
        }
    }

    public List<ReviewData> getReviewsInStates(List<State> states, boolean details) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);
        sb.append(REVIEW_SERVICE);
        if (details) {
            sb.append(DETAIL_REVIEW_INFO);
        }
        if (states != null && states.size() != 0) {
            sb.append(REVIEWS_IN_STATES);
            for (Iterator<State> stateIterator = states.iterator(); stateIterator.hasNext();) {
                State state = stateIterator.next();
                sb.append(state.value());
                if (stateIterator.hasNext()) {
                    sb.append(",");
                }
            }
        }

        try {
            Document doc = retrieveGetResponse(sb.toString());

            XPath xpath;
            if (details) {
                xpath = XPath.newInstance("/detailedReviews/detailReviewData");
            } else {
                xpath = XPath.newInstance("/reviews/reviewData");
            }
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<ReviewData> reviews = new ArrayList<ReviewData>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    if (details) {
                        reviews.add(CrucibleRestXmlHelper.parseDetailedReviewNode(getServer(), element));
                    } else {
                        reviews.add(CrucibleRestXmlHelper.parseReviewNode(getServer(), element));
                    }
                }
            }
            return reviews;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<ReviewData> getAllReviews(boolean details) throws RemoteApiException {
        return getReviewsInStates(null, details);
    }

    public List<ReviewData> getReviewsForFilter(PredefinedFilter filter, boolean details) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        try {
            String url = baseUrl
                    + REVIEW_SERVICE
                    + FILTERED_REVIEWS
                    + "/" + filter.getFilterUrl();
            if (details) {
                url += DETAIL_REVIEW_INFO;   
            }
            Document doc = retrieveGetResponse(url);

            XPath xpath;
            if (details) {
                xpath = XPath.newInstance("/detailedReviews/detailReviewData");
            } else {
                xpath = XPath.newInstance("/reviews/reviewData");
            }
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<ReviewData> reviews = new ArrayList<ReviewData>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    if (details) {
                        reviews.add(CrucibleRestXmlHelper.parseDetailedReviewNode(getServer(), element));
                    } else {
                        reviews.add(CrucibleRestXmlHelper.parseReviewNode(getServer(), element));
                    }
                }
            }
            return reviews;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<ReviewData> getReviewsForCustomFilter(CustomFilter filter, boolean details) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }
        Document request = CrucibleRestXmlHelper.prepareCustomFilter(filter);

        try {
            String url = baseUrl + REVIEW_SERVICE + FILTERED_REVIEWS;
            if (details) {
                url += DETAIL_REVIEW_INFO;
            }
            
            Document doc = retrievePostResponse(url, request);
            XPath xpath;
            if (details) {
                xpath = XPath.newInstance("/detailedReviews/detailReviewData");
            } else {
                xpath = XPath.newInstance("/reviews/reviewData");
            }
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<ReviewData> reviews = new ArrayList<ReviewData>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    if (details) {
                        reviews.add(CrucibleRestXmlHelper.parseDetailedReviewNode(getServer(), element));
                    } else {
                        reviews.add(CrucibleRestXmlHelper.parseReviewNode(getServer(), element));
                    }  
                }
            }
            return reviews;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    private void printXml(Document request) {
        XMLOutputter o = new XMLOutputter(Format.getPrettyFormat());
        try {
            o.output(request, System.out);
        } catch (IOException e) {
            // nothing to do - debug code
        }
    }

    public List<Reviewer> getReviewers(PermId permId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + REVIEWERS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/reviewers/reviewer");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<Reviewer> reviewers = new ArrayList<Reviewer>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    reviewers.add(CrucibleRestXmlHelper.parseReviewerNode(element));
                }
            }
            return reviewers;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<User> getUsers() throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + USER_SERVICE;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/users/userData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<User> users = new ArrayList<User>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    users.add(CrucibleRestXmlHelper.parseUserNode(element));
                }
            }
            return users;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<Project> getProjects() throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + PROJECTS_SERVICE;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/projects/projectData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<Project> projects = new ArrayList<Project>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    projects.add(CrucibleRestXmlHelper.parseProjectNode(element));
                }
            }
            return projects;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<Repository> getRepositories() throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REPOSITORIES_SERVICE;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/repositories/repoData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<Repository> repositories = new ArrayList<Repository>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    repositories.add(CrucibleRestXmlHelper.parseRepositoryNode(element));
                }
            }
            return repositories;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public SvnRepository getRepository(String repoName) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        List<Repository> repositories = getRepositories();
        for (Repository repository : repositories) {
            if (repository.getName().equals(repoName)) {
                if (repository.getType().equals("svn")) {
                    String requestUrl = baseUrl + REPOSITORIES_SERVICE + "/" + repoName + "/svn";
                    try {
                        Document doc = retrieveGetResponse(requestUrl);
                        XPath xpath = XPath.newInstance("/svnRepositoryData");
                        @SuppressWarnings("unchecked")
                        List<Element> elements = xpath.selectNodes(doc);
                        if (elements != null && !elements.isEmpty()) {
                            for (Element element : elements) {
                                return CrucibleRestXmlHelper.parseSvnRepositoryNode(element);
                            }
                        }
                    } catch (IOException e) {
                        throw new RemoteApiException(e.getMessage(), e);
                    } catch (JDOMException e) {
                        throw new RemoteApiException("Server returned malformed response", e);
                    }
                }
            }
        }
        return null;
    }

    public List<CrucibleFileInfo> getFiles(PermId id) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + REVIEW_ITEMS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("reviewItems/reviewItem");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<CrucibleFileInfo> reviewItems = new ArrayList<CrucibleFileInfo>();

			ReviewData changeSet = new ReviewDataImpl(getServer());
			if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    CrucibleFileInfo fileInfo = CrucibleRestXmlHelper.parseReviewItemNode(changeSet, element);
                    String repoName = changeSet.getRepoName();
                    String[] repoNameTokens = repoName.split(":");
                    SvnRepository repository = getRepository(repoNameTokens.length > 1 ? repoNameTokens[1] : repoNameTokens[0]);
                    if (repository != null) {
                        String repoPath = repository.getUrl() + "/" + repository.getPath() + "/";
						VersionedVirtualFile oldDescriptor = fileInfo.getOldFileDescriptor();
						if (!oldDescriptor.getUrl().equals("")) {
							((CrucibleFileInfoImpl) fileInfo).setOldFileDescriptor(new VersionedVirtualFile(
								repoPath + oldDescriptor.getUrl(),
									oldDescriptor.getRevision(),
									oldDescriptor.getFileSystem()
							));
						}
						VersionedVirtualFile newDescriptor = fileInfo.getFileDescriptor();
						if (!newDescriptor.getUrl().equals("")) {
							((CrucibleFileInfoImpl) fileInfo).setFileDescriptor(new VersionedVirtualFile(
								repoPath + newDescriptor.getUrl(),
									newDescriptor.getRevision(),
									newDescriptor.getFileSystem()
							));
						}
                        reviewItems.add(fileInfo);
                    }
                }
            }
            return reviewItems;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<GeneralComment> getGeneralComments(PermId id) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + GENERAL_COMMENTS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("comments/generalCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<GeneralComment> comments = new ArrayList<GeneralComment>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    comments.add(CrucibleRestXmlHelper.parseGeneralCommentNode(element));
                }
            }
            return comments;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<VersionedComment> getAllVersionedComments(PermId id) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + VERSIONED_COMMENTS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("comments/versionedLineCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<VersionedComment> comments = new ArrayList<VersionedComment>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    comments.add(CrucibleRestXmlHelper.parseVersionedCommentNode(element));
                }
            }
            return comments;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<VersionedComment> getVersionedComments(PermId id, PermId reviewItemId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + REVIEW_ITEMS + "/" + reviewItemId.getId() + COMMENTS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("comments/versionedLineCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<VersionedComment> comments = new ArrayList<VersionedComment>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    comments.add(CrucibleRestXmlHelper.parseVersionedCommentNode(element));
                }
            }
            return comments;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<GeneralComment> getReplies(PermId id, PermId commentId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + COMMENTS + "/" + commentId.getId() + REPLIES;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("comments/generalCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<GeneralComment> comments = new ArrayList<GeneralComment>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    comments.add(CrucibleRestXmlHelper.parseGeneralCommentNode(element));
                }
            }
            return comments;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<GeneralComment> getComments(PermId id) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + COMMENTS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("comments/generalCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<GeneralComment> comments = new ArrayList<GeneralComment>();

            if (elements != null && !elements.isEmpty()) {
                int i = 1;
                for (Element element : elements) {
                    GeneralCommentBean comment = CrucibleRestXmlHelper.parseGeneralCommentNode(element);
                    XPath repliesPath = XPath.newInstance("comments/generalCommentData[" + (i++)
                            + "]/replies/generalCommentData");
                    List<Element> replies = repliesPath.selectNodes(doc);
                    if (replies != null && !replies.isEmpty()) {
                        for (Element reply : replies) {
                            comment.addReply(CrucibleRestXmlHelper.parseGeneralCommentNode(reply));
                        }
                    }
                    comments.add(comment);
                }
            }

            xpath = XPath.newInstance("comments/versionedLineCommentData");
            @SuppressWarnings("unchecked")
            List<Element> vElements = xpath.selectNodes(doc);

            if (vElements != null && !vElements.isEmpty()) {
                int i = 1;
                for (Element element : vElements) {
                    VersionedCommentBean comment = CrucibleRestXmlHelper.parseVersionedCommentNode(element);
                    XPath repliesPath = XPath.newInstance("comments/versionedLineCommentData[" + (i++)
                            + "]/replies/generalCommentData");
                    List<Element> replies = repliesPath.selectNodes(doc);
                    if (replies != null && !replies.isEmpty()) {
                        for (Element reply : replies) {
                            comment.addReply(CrucibleRestXmlHelper.parseGeneralCommentNode(reply));
                        }
                    }
                    comments.add(comment);
                }
            }

            return comments;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public GeneralComment addGeneralComment(PermId id, GeneralComment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        Document request = CrucibleRestXmlHelper.prepareGeneralComment(comment);

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + COMMENTS;
        try {
            Document doc = retrievePostResponse(requestUrl, request);

            XPath xpath = XPath.newInstance("generalCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    return CrucibleRestXmlHelper.parseGeneralCommentNode(element);
                }
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public void removeGeneralComment(PermId id, GeneralComment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }
        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + COMMENTS + "/" + comment.getPermId().getId();
        try {
            retrieveDeleteResponse(requestUrl, false);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public void updateGeneralComment(PermId id, GeneralComment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        Document request = CrucibleRestXmlHelper.prepareGeneralComment(comment);
        printXml(request);
        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + COMMENTS + "/" + comment.getPermId().getId();

        try {
            retrievePostResponse(requestUrl, request, false);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public void publishComment(PermId reviewId, PermId commentId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + reviewId.getId() + PUBLISH_COMMENTS;
        if (commentId != null) {
            requestUrl += "/" + commentId.getId();
        }

        try {
            retrievePostResponse(requestUrl, "", false);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    public VersionedComment addVersionedComment(PermId id, VersionedComment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        Document request = CrucibleRestXmlHelper.prepareVersionedComment(comment);

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + REVIEW_ITEMS + "/"
                + comment.getReviewItemId().getId() + COMMENTS;
        try {
            Document doc = retrievePostResponse(requestUrl, request);

            XPath xpath = XPath.newInstance("versionedLineCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    return CrucibleRestXmlHelper.parseVersionedCommentNode(element);
                }
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public GeneralComment addReply(PermId id, PermId cId, GeneralComment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        Document request = CrucibleRestXmlHelper.prepareGeneralComment(comment);

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + COMMENTS + "/" + cId.getId() + REPLIES;

        try {
            Document doc = retrievePostResponse(requestUrl, request);

            XPath xpath = XPath.newInstance("generalCommentData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    return CrucibleRestXmlHelper.parseGeneralCommentNode(element);
                }
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public void updateReply(PermId id, PermId cId, PermId rId, GeneralComment comment) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        Document request = CrucibleRestXmlHelper.prepareGeneralComment(comment);

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + id.getId() + COMMENTS + "/"
                + cId.getId() + REPLIES + "/" + rId.getId();

        try {
            retrievePostResponse(requestUrl, request, false);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public Review createReview(Review review) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }
        return createReviewFromPatch(review, null);
    }

    public Review createReviewFromPatch(Review review, String patch) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        Document request = CrucibleRestXmlHelper.prepareCreateReviewNode(review, patch);
        try {
            Document doc = retrievePostResponse(baseUrl + REVIEW_SERVICE, request);

            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return CrucibleRestXmlHelper.parseReviewNode(getServer(), elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public Review createReviewFromRevision(Review review, List<String> revisions) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        Document request = CrucibleRestXmlHelper.prepareCreateReviewNode(review, revisions);
        try {
            Document doc = retrievePostResponse(baseUrl + REVIEW_SERVICE, request);

            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return CrucibleRestXmlHelper.parseReviewNode(getServer(), elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<CrucibleAction> getAvailableActions(PermId permId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + ACTIONS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/actions/actionData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<CrucibleAction> crucibleActions = new ArrayList<CrucibleAction>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    crucibleActions.add(CrucibleRestXmlHelper.parseActionNode(element));
                }
            }
            return crucibleActions;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<Transition> getAvailableTransitions(PermId permId) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + TRANSITIONS;
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("/transitions/transitionData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<Transition> transitions = new ArrayList<Transition>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    transitions.add(CrucibleRestXmlHelper.parseTransitionNode(element));
                }
            }
            return transitions;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public Review addRevisionsToReview(PermId permId, String repository, List<String> revisions) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        Document request = CrucibleRestXmlHelper.prepareAddChangesetNode(repository, revisions);

        try {
            String url = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + ADD_CHANGESET;
            Document doc = retrievePostResponse(url, request);


            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return CrucibleRestXmlHelper.parseReviewNode(getServer(), elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public Review addPatchToReview(PermId permId, String repository, String patch) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        Document request = CrucibleRestXmlHelper.prepareAddPatchNode(repository, patch);

        try {
            String url = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + ADD_PATCH;
            Document doc = retrievePostResponse(url, request);

            XPath xpath = XPath.newInstance("/reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);

            if (elements != null && !elements.isEmpty()) {
                return CrucibleRestXmlHelper.parseReviewNode(getServer(), elements.iterator().next());
            }
            return null;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public void addReviewers(PermId permId, Set<String> users) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + REVIEWERS;
        String reviewers = "";
        for (String user : users) {
            if (reviewers.length() > 0) {
                reviewers += ",";
            }
            reviewers += user;
        }

        try {
            retrievePostResponse(requestUrl, reviewers, false);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    private Review changeReviewState(PermId permId, String action) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + TRANSITION_ACTION + action;
        try {
            Document doc = retrievePostResponse(requestUrl, "", true);

            XPath xpath = XPath.newInstance("reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            Review review = null;

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    review = CrucibleRestXmlHelper.parseReviewNode(getServer(), element);
                }
            }
            return review;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public void completeReview(PermId permId, boolean complete) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId();
        if (complete) {
            requestUrl += COMPLETE_ACTION;
        } else {
            requestUrl += UNCOMPLETE_ACTION;
        }

        try {
            retrievePostResponse(requestUrl, "", false);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public Review approveReview(PermId permId) throws RemoteApiException {
        return changeReviewState(permId, APPROVE_ACTION);
    }

    public Review abandonReview(PermId permId) throws RemoteApiException {
        return changeReviewState(permId, ABANDON_ACTION);
    }

    public Review summarizeReview(PermId permId) throws RemoteApiException {
        return changeReviewState(permId, SUMMARIZE_ACTION);
    }

    public Review recoverReview(PermId permId) throws RemoteApiException {
        return changeReviewState(permId, RECOVER_ACTION);
    }

    public Review reopenReview(PermId permId) throws RemoteApiException {
        return changeReviewState(permId, REOPEN_ACTION);
    }

    public Review rejectReview(PermId permId) throws RemoteApiException {
        return changeReviewState(permId, REJECT_ACTION);
    }

    public Review closeReview(PermId permId, String summarizeMessage) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        try {
            Document doc;
            if (summarizeMessage != null && !"".equals(summarizeMessage)) {
                Document request = CrucibleRestXmlHelper.prepareCloseReviewSummaryNode(summarizeMessage);
                String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + "/close";
                doc = retrievePostResponse(requestUrl, request);
            } else {
                String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + TRANSITION_ACTION + CLOSE_ACTION;
                doc = retrievePostResponse(requestUrl, "", true);
            }

            XPath xpath = XPath.newInstance("reviewData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            Review review = null;

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    review = CrucibleRestXmlHelper.parseReviewNode(getServer(), element);
                }
            }
            return review;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    public List<CustomFieldDef> getMetrics(int version) throws RemoteApiException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Calling method without calling login() first");
        }

        String requestUrl = baseUrl + REVIEW_SERVICE + METRICS + "/" + Integer.toString(version);
        try {
            Document doc = retrieveGetResponse(requestUrl);

            XPath xpath = XPath.newInstance("metrics/metricsData");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            List<CustomFieldDef> metrics = new ArrayList<CustomFieldDef>();

            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    metrics.add(CrucibleRestXmlHelper.parseMetricsNode(element));
                }
            }
            return metrics;
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        }
    }

    protected void adjustHttpHeader(HttpMethod method) {
        method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));
    }

    protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {

    }

    private String getAuthHeaderValue() {
        return "Basic " + encode(userName + ":" + password);
    }

    private synchronized String encode(String str2encode) {
        try {
            return Base64.encodeBytes(str2encode.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is not supported", e);
        }
    }

    private static String getExceptionMessages(Document doc) throws JDOMException {
        XPath xpath = XPath.newInstance("/loginResult/error");
        @SuppressWarnings("unchecked")
        List<Element> elements = xpath.selectNodes(doc);

        if (elements != null && elements.size() > 0) {
            StringBuffer exceptionMsg = new StringBuffer();
            for (Element e : elements) {
                exceptionMsg.append(e.getText());
                exceptionMsg.append("\n");
            }
            return exceptionMsg.toString();
        } else {
            /* no exception */
            return null;
        }
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }
}