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

import com.atlassian.theplugin.commons.crucible.api.*;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public final class CrucibleRestXmlHelper {

    ///CLOVER:OFF
    private CrucibleRestXmlHelper() {
    }
    ///CLOVER:ON

    public static String getChildText(Element node, String childName) {
        try {
            return node.getChild(childName).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public static ProjectDataBean parseProjectNode(Element projectNode) {
        ProjectDataBean project = new ProjectDataBean();

        project.setId(getChildText(projectNode, "id"));
        project.setKey(getChildText(projectNode, "key"));
        project.setName(getChildText(projectNode, "name"));

        return project;
    }

    public static RepositoryDataBean parseRepositoryNode(Element repoNode) {
        RepositoryDataBean repo = new RepositoryDataBean();
        repo.setName(getChildText(repoNode, "name"));
        repo.setType(getChildText(repoNode, "type"));
        repo.setEnabled(Boolean.parseBoolean(getChildText(repoNode, "enabled")));
        return repo;
    }

    public static SvnRepositoryDataBean parseSvnRepositoryNode(Element repoNode) {
        SvnRepositoryDataBean repo = new SvnRepositoryDataBean();
        repo.setName(getChildText(repoNode, "name"));
        repo.setType(getChildText(repoNode, "type"));
        repo.setEnabled(Boolean.parseBoolean(getChildText(repoNode, "enabled")));
        repo.setUrl(getChildText(repoNode, "url"));
        repo.setPath(getChildText(repoNode, "path"));
        return repo;
    }

    public static UserDataBean parseUserNode(Element repoNode) {
        UserDataBean userDataBean = new UserDataBean();

        CrucibleVersion version = CrucibleVersion.CRUCIBLE_15;
		Element userName = repoNode.getChild("userName");
		if (userName != null && !userName.getText().equals("")) {
            version = CrucibleVersion.CRUCIBLE_16;
        }
          if (version == CrucibleVersion.CRUCIBLE_15) {
            userDataBean.setUserName(repoNode.getText());
            userDataBean.setDisplayName(userDataBean.getUserName());
        } else {
            userDataBean.setUserName(getChildText(repoNode, "userName"));
            userDataBean.setDisplayName(getChildText(repoNode, "displayName"));
        }
        return userDataBean;
    }
        
    public static ReviewData parseReviewNode(Element reviewNode) {
        ReviewDataBean review = new ReviewDataBean();

        review.setAuthor(getChildText(reviewNode, "author"));
        review.setCreator(getChildText(reviewNode, "creator"));
        review.setModerator(getChildText(reviewNode, "moderator"));
        review.setDescription(getChildText(reviewNode, "description"));
        review.setName(getChildText(reviewNode, "name"));
        review.setProjectKey(getChildText(reviewNode, "projectKey"));
        review.setRepoName(getChildText(reviewNode, "repoName"));

        String stateString = getChildText(reviewNode, "state");
        if (!"".equals(stateString)) {
            review.setState(State.fromValue(stateString));
        }

        if (reviewNode.getChild("permaId") != null) {
            PermIdBean permId = new PermIdBean();
            permId.setId(reviewNode.getChild("permaId").getChild("id").getText());
            review.setPermaId(permId);
        }

        return review;
    }

    public static void addTag(Element root, String tagName, String tagValue) {
        Element newElement = new Element(tagName);
        newElement.addContent(tagValue);
        root.getContent().add(newElement);
    }

    public static Document prepareCreateReviewNode(ReviewData review, String patch) {
        Element root = new Element("createReview");
        Document doc = new Document(root);

        root.getContent().add(prepareReviewNodeElement(review));

        if (patch != null) {
            Element patchData = new Element("patch");
            root.getContent().add(patchData);

            CDATA patchT = new CDATA(patch);
            patchData.setContent(patchT);
        }
        return doc;
    }

    public static Document prepareCreateReviewNode(ReviewData review, List<String> revisions) {
        Element root = new Element("createReview");
        Document doc = new Document(root);

        root.getContent().add(prepareReviewNodeElement(review));

        if (!revisions.isEmpty()) {
            if (revisions.size() == 1) {
                addTag(root, "changeSetId", revisions.get(0));
            } else {
                Element changes = new Element("revisionId");
                root.getContent().add(changes);
                for (String revision : revisions) {
                    Element rev = new Element("revisionData");
                    changes.getContent().add(rev);
                    addTag(rev, "id", revision);
                }
            }
        }
        return doc;
    }

    public static Document prepareAddChangesetNode(String repoName, List<String> revisions) {
        Element root = new Element("addChangeset");
        Document doc = new Document(root);

        addTag(root, "repository", repoName);

        if (!revisions.isEmpty()) {
                Element changes = new Element("revisionId");
                root.getContent().add(changes);
                for (String revision : revisions) {
                    Element rev = new Element("revisionData");
                    changes.getContent().add(rev);
                    addTag(rev, "id", revision);
                }
        }
        return doc;
    }

    public static Document prepareAddReviewerNode(String userName) {
        Element root = new Element("reviewer");
        Document doc = new Document(root);

        addTag(root, "reviewer", userName);
        return doc;
    }

    public static Document prepareReviewNode(ReviewData review) {
        Element reviewData = prepareReviewNodeElement(review);
        return new Document(reviewData);
    }

    private static Element prepareReviewNodeElement(ReviewData review) {
        Element reviewData = new Element("reviewData");

        addTag(reviewData, "author", review.getAuthor());
        addTag(reviewData, "creator", review.getCreator());
        addTag(reviewData, "description", review.getDescription());
        addTag(reviewData, "moderator", review.getModerator());
        addTag(reviewData, "name", review.getName());
        addTag(reviewData, "projectKey", review.getProjectKey());
        addTag(reviewData, "repoName", review.getRepoName());
        if (review.getState() != null) {
            addTag(reviewData, "state", review.getState().value());
        }
        if (review.getPermaId() != null) {
            Element permIdElement = new Element("permaId");
            reviewData.getContent().add(permIdElement);
            addTag(permIdElement, "id", review.getPermaId().getId());
        }

        return reviewData;
    }

    public static ReviewItemDataBean parseReviewItemNode(Element reviewItemNode) {
        ReviewItemDataBean reviewItem = new ReviewItemDataBean();

        reviewItem.setFromPath(getChildText(reviewItemNode, "fromPath"));
        reviewItem.setFromRevision(getChildText(reviewItemNode, "fromRevision"));
        reviewItem.setToPath(getChildText(reviewItemNode, "toPath"));
        reviewItem.setToRevision(getChildText(reviewItemNode, "toRevision"));
        reviewItem.setRepositoryName(getChildText(reviewItemNode, "repositoryName"));
        if (reviewItemNode.getChild("permaId") != null) {
            PermIdBean permId = new PermIdBean();
            permId.setId(reviewItemNode.getChild("permaId").getChild("id").getText());
            reviewItem.setPermId(permId);
        }

        return reviewItem;
    }

    public static GeneralCommentBean parseGeneralCommentNode(Element reviewCommentNode) {
        GeneralCommentBean commentBean = new GeneralCommentBean();

        commentBean.setUser(getChildText(reviewCommentNode, "user"));
        commentBean.setDisplayUser(getChildText(reviewCommentNode, "userDisplayName"));
        commentBean.setMessage(getChildText(reviewCommentNode, "message"));
        commentBean.setDefectRaised(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectRaised")));
        commentBean.setDefectApproved(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectApproved")));
        commentBean.setDraft(Boolean.parseBoolean(getChildText(reviewCommentNode, "draft")));
        commentBean.setDeleted(Boolean.parseBoolean(getChildText(reviewCommentNode, "deleted")));
        commentBean.setCreateDate(parseCommentTime(getChildText(reviewCommentNode, "createDate")));

        return commentBean;
    }

    public static VersionedCommentBean parseVersionedCommentNode(Element reviewCommentNode) {
        VersionedCommentBean comment = new VersionedCommentBean();

        comment.setUser(getChildText(reviewCommentNode, "user"));
        comment.setDisplayUser(getChildText(reviewCommentNode, "userDisplayName"));
        comment.setMessage(getChildText(reviewCommentNode, "message"));
        comment.setDefectRaised(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectRaised")));
        comment.setDefectApproved(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectApproved")));
        comment.setDraft(Boolean.parseBoolean(getChildText(reviewCommentNode, "draft")));
        comment.setDeleted(Boolean.parseBoolean(getChildText(reviewCommentNode, "deleted")));
        comment.setCreateDate(parseCommentTime(getChildText(reviewCommentNode, "createDate")));

        if (reviewCommentNode.getChild("permaId") != null) {
            PermIdBean permId = new PermIdBean();
            permId.setId(reviewCommentNode.getChild("permaId").getChild("id").getText());
            comment.setPermId(permId);
        }

        if (reviewCommentNode.getChild("reviewItemId") != null) {
            ReviewItemIdBean reviewItemId = new ReviewItemIdBean();
            reviewItemId.setId(reviewCommentNode.getChild("reviewItemId").getChild("id").getText());
            comment.setReviewItemId(reviewItemId);
        }

        if (reviewCommentNode.getChild("fromLineRange") != null) {
            String toLineRange = getChildText(reviewCommentNode, "fromLineRange");
            String[] tokens = toLineRange.split("-");
            if (tokens.length > 0) {
                comment.setFromLineInfo(true);
                try {
                    int start = Integer.parseInt(tokens[0]);
                    comment.setFromStartLine(start);
                } catch (NumberFormatException e) {
                    // leave 0 value
                }
                if (tokens.length > 1) {
                    try {
                        int stop = Integer.parseInt(tokens[1]);
                        comment.setFromEndLine(stop);
                    } catch (NumberFormatException e) {
                        // leave 0 value
                    }
                }
            }
        }

        if (reviewCommentNode.getChild("toLineRange") != null) {
            String toLineRange = getChildText(reviewCommentNode, "toLineRange");
            String[] tokens = toLineRange.split("-");
            if (tokens.length > 0) {
                comment.setToLineInfo(true);
                try {
                    int start = Integer.parseInt(tokens[0]);
                    comment.setToStartLine(start);
                } catch (NumberFormatException e) {
                    // leave 0 value
                }
                if (tokens.length > 1) {
                    try {
                        int stop = Integer.parseInt(tokens[1]);
                        comment.setToEndLine(stop);
                    } catch (NumberFormatException e) {
                        // leave 0 value
                    }
                }
            }
        }

        return comment;
    }

    public static Document prepareCustomFilter(CustomFilter filter) {
        Element filterData = prepareFilterNodeElement(filter);
        return new Document(filterData);
    }

    private static Element prepareFilterNodeElement(CustomFilter filter) {
        Element filterData = new Element("customFilterData");

        addTag(filterData, "title", /*filter.getTitle() != null ? filter.getTitle() :*/ "");        
        addTag(filterData, "author", filter.getAuthor() != null ? filter.getAuthor() : "");
        addTag(filterData, "creator", filter.getCreator() != null ? filter.getCreator() : "");
        addTag(filterData, "moderator", filter.getModerator() != null ? filter.getModerator() : "");
        addTag(filterData, "reviewer", filter.getReviewer() != null ? filter.getReviewer() : "");
        addTag(filterData, "projectKey", filter.getProjectKey() != null ? filter.getProjectKey() : "");
        String state = "";
        if (filter.getState() != null) {
            for (String s : filter.getState()) {
                if (state.length() > 0) {
                    state += ",";
                }
                state += s;
            }
        }
        addTag(filterData, "state", state);
        addTag(filterData, "complete", filter.isComplete() ? "true" : "false");
        addTag(filterData, "orRoles", filter.isOrRoles() ? "true" : "false");
        addTag(filterData, "allReviewersComplete", filter.isAllReviewersComplete() ? "true" : "false");

        return filterData;
    }


    private static SimpleDateFormat commentTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");

    private static Date parseCommentTime(String date) {
        try {
            date = date.replace(":00", "00");
            return commentTimeFormat.parse(date);
        } catch (ParseException e) {
            System.out.println("e = " + e);
            return null;
        }
    }

}
