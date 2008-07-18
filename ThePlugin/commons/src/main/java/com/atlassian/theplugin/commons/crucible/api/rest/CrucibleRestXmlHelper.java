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

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public static List<Element> getChildElements(Element node, String childName) {
        try {
            return node.getChildren(childName);
        } catch (Exception e) {
            System.out.println("e = " + e);
            return null;
        }

    }

    public static ProjectBean parseProjectNode(Element projectNode) {
        ProjectBean project = new ProjectBean();

        project.setId(getChildText(projectNode, "id"));
        project.setKey(getChildText(projectNode, "key"));
        project.setName(getChildText(projectNode, "name"));

        return project;
    }

    public static RepositoryBean parseRepositoryNode(Element repoNode) {
        RepositoryBean repo = new RepositoryBean();
        repo.setName(getChildText(repoNode, "name"));
        repo.setType(getChildText(repoNode, "type"));
        repo.setEnabled(Boolean.parseBoolean(getChildText(repoNode, "enabled")));
        return repo;
    }

    public static SvnRepositoryBean parseSvnRepositoryNode(Element repoNode) {
        SvnRepositoryBean repo = new SvnRepositoryBean();
        repo.setName(getChildText(repoNode, "name"));
        repo.setType(getChildText(repoNode, "type"));
        repo.setEnabled(Boolean.parseBoolean(getChildText(repoNode, "enabled")));
        repo.setUrl(getChildText(repoNode, "url"));
        repo.setPath(getChildText(repoNode, "path"));
        return repo;
    }

    public static UserBean parseUserNode(Element repoNode) {
        UserBean userDataBean = new UserBean();

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

    public static Action parseActionNode(Element element) {
        return Action.fromValue(getChildText(element, "name"));
    }

    public static Transition parseTransitionNode(Element element) {
        TransitionBean transitionBean = new TransitionBean();

        transitionBean.setActionName(getChildText(element, "actionName"));
        transitionBean.setDisplayName(getChildText(element, "displayName"));
        String stateString = getChildText(element, "state");
        if (!"".equals(stateString)) {
            transitionBean.setState(State.fromValue(stateString));
        }
        stateString = getChildText(element, "nextState");
        if (!"".equals(stateString)) {
            transitionBean.setNextState(State.fromValue(stateString));
        }

        return transitionBean;
    }


    public static ReviewerBean parseReviewerNode(Element reviewerNode) {
        ReviewerBean reviewerBean = new ReviewerBean();

        CrucibleVersion version = CrucibleVersion.CRUCIBLE_15;
        Element userName = reviewerNode.getChild("userName");
        if (userName != null && !userName.getText().equals("")) {
            version = CrucibleVersion.CRUCIBLE_16;
        }
        if (version == CrucibleVersion.CRUCIBLE_15) {
            reviewerBean.setUserName(reviewerNode.getText());
            reviewerBean.setDisplayName(reviewerBean.getUserName());
        } else {
            reviewerBean.setUserName(getChildText(reviewerNode, "userName"));
            reviewerBean.setDisplayName(getChildText(reviewerNode, "displayName"));
            reviewerBean.setCompleted(Boolean.parseBoolean(getChildText(reviewerNode, "completed")));
        }
        return reviewerBean;
    }

    private static void parseReview(Element reviewNode, ReviewBean review) {
        if (reviewNode.getChild("author") != null) {
            review.setAuthor(parseUserNode(reviewNode.getChild("author")));
        }
        if (reviewNode.getChild("creator") != null) {
            review.setCreator(parseUserNode(reviewNode.getChild("creator")));
        }
        if (reviewNode.getChild("moderator") != null) {
            review.setModerator(parseUserNode(reviewNode.getChild("moderator")));
        }
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
            review.setPermId(permId);
        }

        try {
            review.setMetricsVersion(Integer.valueOf(getChildText(reviewNode, "metricsVersion")));
        } catch (NumberFormatException e) {
            review.setMetricsVersion(-1);
        }
    }

    public static ReviewBean parseReviewNode(Element reviewNode) {
        ReviewBean review = new ReviewBean();
        parseReview(reviewNode, review);
        return review;
    }

    public static ReviewBean parseDetailedReviewNode(Element reviewNode) {
        ReviewBean review = new ReviewBean();
        parseReview(reviewNode, review);

        List<Element> reviewersNode = getChildElements(reviewNode, "reviewers");
        List<Reviewer> reviewers = new ArrayList<Reviewer>();
        for (Element reviewer : reviewersNode) {
            List<Element> reviewerNode = getChildElements(reviewer, "reviewer");
            for (Element element : reviewerNode) {
                reviewers.add(parseReviewerNode(element));
            }
        }
        review.setReviewers(reviewers);

        List<Element> reviewItemsNode = getChildElements(reviewNode, "reviewItems");
        for (Element reviewItem : reviewItemsNode) {
            List<Element> itemNode = getChildElements(reviewItem, "reviewItem");
            List<CrucibleFileInfo> files = new ArrayList<CrucibleFileInfo>();
            for (Element element : itemNode) {
                files.add(parseReviewItemNode(review, element));
            }
            review.setFiles(files);
        }

        List<Element> generalCommentsNode = getChildElements(reviewNode, "generalComments");
        for (Element generalComment : generalCommentsNode) {
            List<Element> commentNode = getChildElements(generalComment, "generalCommentData");
            List<GeneralComment> generalComments = new ArrayList<GeneralComment>();
            for (Element element : commentNode) {
                generalComments.add(parseGeneralCommentNode(element));
            }
            review.setGeneralComments(generalComments);
        }

        List<Element> versionedComments = getChildElements(reviewNode, "versionedComments");
        for (Element versionedComment : versionedComments) {
            List<Element> commentNode = getChildElements(versionedComment, "versionedLineCommentData");
            List<VersionedCommentBean> comments = new ArrayList<VersionedCommentBean>();
            for (Element element : commentNode) {
                comments.add(parseVersionedCommentNode(element));
            }


            try {
                for (CrucibleFileInfo item : review.getFiles()) {
                    List<VersionedComment> commentList = new ArrayList<VersionedComment>();
                    for (VersionedCommentBean comment : comments) {
                        if (item.getPermId().getId().equals(comment.getReviewItemId().getId())) {
                            commentList.add(comment);
                        }
                    }
                    ((CrucibleFileInfoImpl) item).setVersionedComments(commentList);
                }
            } catch (ValueNotYetInitialized ex) {
                // ignore, because it cannot happen as setFiles is invoked a few lines higher
            }

        }

        List<Element> transitionsNode = getChildElements(reviewNode, "transitions");
        List<Transition> transitions = new ArrayList<Transition>();
        for (Element transition : transitionsNode) {
            List<Element> trans = getChildElements(transition, "transitionData");
            for (Element element : trans) {
                transitions.add(parseTransitionNode(element));
            }
        }
        review.setTransitions(transitions);

        List<Element> actionsNode = getChildElements(reviewNode, "actions");
        List<Action> actions = new ArrayList<Action>();
        for (Element action : actionsNode) {
            List<Element> act = getChildElements(action, "actionData");
            for (Element element : act) {
                actions.add(parseActionNode(element));
            }
        }
        review.setActions(actions);

        return review;
    }

    public static Element addTag(Element root, String tagName, String tagValue) {
        Element newElement = new Element(tagName);
        newElement.addContent(tagValue);
        root.getContent().add(newElement);
        return newElement;
    }

    public static Document prepareCreateReviewNode(Review review, String patch) {
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

    public static Document prepareCloseReviewSummaryNode(String message) {
        Element root = new Element("closeReviewSummary");
        Document doc = new Document(root);

        if (message != null) {
            Element messageData = new Element("summary");
            root.getContent().add(messageData);

            CDATA patchT = new CDATA(message);
            messageData.setContent(patchT);
        }
        return doc;
    }

    public static Document prepareCreateReviewNode(Review review, List<String> revisions) {
        Element root = new Element("createReview");
        Document doc = new Document(root);

        root.getContent().add(prepareReviewNodeElement(review));

        if (!revisions.isEmpty()) {
            Element changes = new Element("changesets");
            root.getContent().add(changes);
            for (String revision : revisions) {
                Element rev = new Element("changesetData");
                changes.getContent().add(rev);
                addTag(rev, "id", revision);
            }
        }
        return doc;
    }

    public static Document prepareAddChangesetNode(String repoName, List<String> revisions) {
        Element root = new Element("addChangeset");
        Document doc = new Document(root);

        addTag(root, "repository", repoName);

        if (!revisions.isEmpty()) {
            Element changes = new Element("changesets");
            root.getContent().add(changes);
            for (String revision : revisions) {
                Element rev = new Element("changesetData");
                changes.getContent().add(rev);
                addTag(rev, "id", revision);
            }
        }
        return doc;
    }

    public static Document prepareAddPatchNode(String repoName, String patch) {
        Element root = new Element("addPatch");
        Document doc = new Document(root);

        addTag(root, "repository", repoName);

        if (patch != null) {
            Element patchData = new Element("patch");
            root.getContent().add(patchData);

            CDATA patchT = new CDATA(patch);
            patchData.setContent(patchT);
        }
        return doc;
    }

    public static Document prepareReviewNode(Review review) {
        Element reviewData = prepareReviewNodeElement(review);
        return new Document(reviewData);
    }

    private static Element prepareReviewNodeElement(Review review) {
        Element reviewData = new Element("reviewData");

        Element authorElement = new Element("author");
        reviewData.getContent().add(authorElement);
        addTag(authorElement, "userName", review.getAuthor().getUserName());

        Element creatorElement = new Element("creator");
        reviewData.getContent().add(creatorElement);
        addTag(creatorElement, "userName", review.getCreator().getUserName());

        Element moderatorElement = new Element("moderator");
        reviewData.getContent().add(moderatorElement);
        addTag(moderatorElement, "userName", review.getModerator().getUserName());

        addTag(reviewData, "description", review.getDescription());
        addTag(reviewData, "name", review.getName());
        addTag(reviewData, "projectKey", review.getProjectKey());
        addTag(reviewData, "repoName", review.getRepoName());
        if (review.getState() != null) {
            addTag(reviewData, "state", review.getState().value());
        }
        if (review.getPermId() != null) {
            Element permIdElement = new Element("permaId");
            reviewData.getContent().add(permIdElement);
            addTag(permIdElement, "id", review.getPermId().getId());
        }

        return reviewData;
    }

    public static CrucibleFileInfo parseReviewItemNode(Review review, Element reviewItemNode) {
        CrucibleFileInfoImpl reviewItem = new CrucibleFileInfoImpl(
                new VersionedVirtualFile(
                        getChildText(reviewItemNode, "toPath"),
                        getChildText(reviewItemNode, "toRevision"),
                        review.getVirtualFileSystem()
                ),
                new VersionedVirtualFile(
                        getChildText(reviewItemNode, "fromPath"),
                        getChildText(reviewItemNode, "fromRevision"),
                        review.getVirtualFileSystem()
                )
        );
        ((ReviewBean) review).setRepoName(getChildText(reviewItemNode, "repositoryName"));
        // todo lguminski to ask Marek about XML sructure
        if (reviewItemNode.getChild("permId") != null) {
            PermIdBean permId = new PermIdBean();
            permId.setId(reviewItemNode.getChild("permId").getChild("id").getText());
            reviewItem.setPermId(permId);
        }

        return reviewItem;
    }

    private static void parseGeneralComment(GeneralCommentBean commentBean, Element reviewCommentNode) {
        parsewComment(commentBean, reviewCommentNode);
        List<Element> replies = getChildElements(reviewCommentNode, "replies");
        if (replies != null) {
            List<GeneralComment> rep = new ArrayList<GeneralComment>();
            for (Element repliesNode : replies) {
                List<Element> entries = getChildElements(repliesNode, "generalCommentData");
                for (Element replyNode : entries) {
                    GeneralCommentBean reply = parseGeneralCommentNode(replyNode);
                    reply.setReply(true);
                    rep.add(reply);
                }
            }
            commentBean.setReplies(rep);
        }
    }

    private static void parseVersionedComment(VersionedCommentBean commentBean, Element reviewCommentNode) {
        parsewComment(commentBean, reviewCommentNode);
        List<Element> replies = getChildElements(reviewCommentNode, "replies");
        if (replies != null) {
            List<VersionedComment> rep = new ArrayList<VersionedComment>();
            for (Element repliesNode : replies) {
                List<Element> entries = getChildElements(repliesNode, "generalCommentData");
                for (Element replyNode : entries) {
                    VersionedCommentBean reply = parseVersionedCommentNodeWithHints(replyNode,
                            commentBean.isFromLineInfo(),
                            commentBean.getFromStartLine(),
                            commentBean.getToStartLine(),
                            commentBean.isToLineInfo(),
                            commentBean.getFromEndLine(),
                            commentBean.getToEndLine()
                    );
                    reply.setReply(true);
                    rep.add(reply);
                }
            }
            commentBean.setReplies(rep);
        }

    }

    private static void parsewComment(CommentBean commentBean, Element reviewCommentNode) {

        commentBean.setUser(getChildText(reviewCommentNode, "user"));
        commentBean.setDisplayUser(getChildText(reviewCommentNode, "userDisplayName"));
        commentBean.setMessage(getChildText(reviewCommentNode, "message"));
        commentBean.setDefectRaised(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectRaised")));
        commentBean.setDefectApproved(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectApproved")));
        commentBean.setDraft(Boolean.parseBoolean(getChildText(reviewCommentNode, "draft")));
        commentBean.setDeleted(Boolean.parseBoolean(getChildText(reviewCommentNode, "deleted")));
        commentBean.setCreateDate(parseCommentTime(getChildText(reviewCommentNode, "createDate")));
        PermIdBean permId = new PermIdBean();
        permId.setId(getChildText(reviewCommentNode, "permaIdAsString"));
        commentBean.setPermId(permId);


        List<Element> metrics = getChildElements(reviewCommentNode, "metrics");
        if (metrics != null) {
            for (Element metric : metrics) {
                List<Element> entries = getChildElements(metric, "entry");
                for (Element entry : entries) {
                    String key = getChildText(entry, "key");
                    List<Element> values = getChildElements(entry, "value");
                    for (Element value : values) {
                        CustomFieldBean field = new CustomFieldBean();
                        field.setConfigVersion(Integer.parseInt(getChildText(value, "configVersion")));
                        field.setValue(getChildText(value, "value"));
                        commentBean.getCustomFields().put(key, field);
                        break;
                    }
                }
            }
        }
    }

    private static void prepareComment(Comment comment, Element commentNode) {
        String date = commentTimeFormat.format(comment.getCreateDate());
        String strangeDate = date.substring(0, date.length() - 2);
        strangeDate += ":00";
        addTag(commentNode, "createDate", strangeDate);
        addTag(commentNode, "user", comment.getUser());
        addTag(commentNode, "defectRaised", Boolean.toString(comment.isDefectRaised()));
        addTag(commentNode, "defectApproved", Boolean.toString(comment.isDefectApproved()));
        addTag(commentNode, "deleted", Boolean.toString(comment.isDeleted()));
        addTag(commentNode, "draft", Boolean.toString(comment.isDraft()));
        addTag(commentNode, "message", comment.getMessage());
        Element metrics = new Element("metrics");
        commentNode.getContent().add(metrics);

        for (String key : comment.getCustomFields().keySet()) {
            Element entry = new Element("entry");
            metrics.getContent().add(entry);
            addTag(entry, "key", key);
            CustomField field = comment.getCustomFields().get(key);
            entry.getContent().add(prepareCustomFieldValue(field));
        }

        Element replies = new Element("replies");
        commentNode.getContent().add(replies);
    }

    public static GeneralCommentBean parseGeneralCommentNode(Element reviewCommentNode) {
        GeneralCommentBean reviewCommentBean = new GeneralCommentBean();
        parseGeneralComment(reviewCommentBean, reviewCommentNode);
        return reviewCommentBean;
    }

    public static Document prepareGeneralComment(GeneralComment comment) {
        Element commentNode = new Element("generalCommentData");
        Document doc = new Document(commentNode);
        prepareComment(comment, commentNode);
        return doc;
    }

    public static Document prepareVersionedComment(VersionedComment comment) {
        Element commentNode = new Element("versionedLineCommentData");
        Document doc = new Document(commentNode);
        prepareComment(comment, commentNode);
        Element reviewItemId = new Element("reviewItemId");
        commentNode.getContent().add(reviewItemId);
        addTag(reviewItemId, "id", comment.getReviewItemId().getId());
        if (comment.getFromStartLine() > 0 && comment.getFromEndLine() > 0) {
            addTag(commentNode, "fromLineRange", comment.getFromStartLine() + "-" + comment.getFromEndLine());
        }
        if (comment.getToStartLine() > 0 && comment.getToEndLine() > 0) {
            addTag(commentNode, "toLineRange", comment.getToStartLine() + "-" + comment.getToEndLine());
        }
        return doc;
    }

    public static VersionedCommentBean parseVersionedCommentNodeWithHints(Element reviewCommentNode,
                                                                          boolean fromLineInfo,
                                                                          int fromStartLine,
                                                                          int toStartLine,
                                                                          boolean toLineInfo,
                                                                          int fromEndLine,
                                                                          int toEndLine) {
        VersionedCommentBean result = parseVersionedCommentNode(reviewCommentNode);
        if (result.isFromLineInfo() == false && fromLineInfo == true) {
            result.setFromLineInfo(true);
            result.setFromStartLine(fromStartLine);
            result.setFromEndLine(fromEndLine);
        }
        if (result.isToLineInfo() == false && toLineInfo == true) {
            result.setToLineInfo(true);
            result.setToStartLine(toStartLine);
            result.setToEndLine(toEndLine);
        }
        return result;
    }

    public static VersionedCommentBean parseVersionedCommentNode(Element reviewCommentNode) {
        VersionedCommentBean comment = new VersionedCommentBean();
        parseVersionedComment(comment, reviewCommentNode);

        if (reviewCommentNode.getChild("reviewItemId") != null) {
            PermIdBean reviewItemId = new PermIdBean();
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

    private static Element prepareCustomFieldValue(CustomField value) {
        Element entry = new Element("value");
        addTag(entry, "configVersion", Integer.toString(value.getConfigVersion()));
        addTag(entry, "value", value.getValue());
        return entry;
    }

    private static CustomFieldValue getCustomFieldValue(CustomFieldValueType type, Element element) {
        CustomFieldValue newValue = new CustomFieldValue();
        newValue.setName(getChildText(element, "name"));
        switch (type) {
            case INTEGER:
                newValue.setValue(Integer.valueOf(getChildText(element, "value")));
                break;
            case STRING:
                newValue.setValue(getChildText(element, "value"));
                break;
            case BOOLEAN:
                newValue.setValue(Boolean.valueOf(getChildText(element, "value")));
                break;
            case DATE:
                // date not set by default at this moment - not sure date representation
            default:
                newValue.setValue(null);
        }
        return newValue;
    }

    public static CustomFieldDefBean parseMetricsNode(Element element) {
        CustomFieldDefBean field = new CustomFieldDefBean();

        field.setName(getChildText(element, "name"));
        field.setLabel(getChildText(element, "label"));
        field.setType(CustomFieldValueType.valueOf(getChildText(element, "type")));
        field.setConfigVersion(Integer.parseInt(getChildText(element, "configVersion")));

        List<Element> defaultValue = getChildElements(element, "defaultValue");
        for (Element value : defaultValue) {
            field.setDefaultValue(getCustomFieldValue(field.getType(), value));
        }
        List<Element> values = getChildElements(element, "values");
        for (Element value : values) {
            field.getValues().add(getCustomFieldValue(field.getType(), value));
        }

        return field;
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

    private static SimpleDateFormat commentTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static Date parseCommentTime(String date) {
        try {
            return commentTimeFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
