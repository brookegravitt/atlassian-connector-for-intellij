package com.atlassian.connector.intellij.stash.impl;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.intellij.stash.Comment;
import com.atlassian.connector.intellij.stash.PullRequest;
import com.atlassian.connector.intellij.stash.StashServerFacade;
import com.atlassian.connector.intellij.stash.StashSession;
import com.atlassian.connector.intellij.stash.beans.ChangeBean;
import com.atlassian.connector.intellij.stash.beans.CommentBean;
import com.atlassian.connector.intellij.stash.beans.PullRequestBean;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StashServerFacadeImpl implements StashServerFacade
{
    private static final Gson gson = new Gson();

    public static final String PROJECT_KEY = "GM";
    public static final String REPO = "gitmilk";

    private Optional<PullRequest> currentPullRequest = Optional.absent();

    private final StashSession stashSession = new StashRestSession();
    private static StashServerFacadeImpl instance;

    static {
        instance = new StashServerFacadeImpl();
    }

    private StashServerFacadeImpl() {
        try {
            stashSession.login("blewandowski", "blewandowski".toCharArray());
        } catch (RemoteApiLoginException e) {
            e.printStackTrace();
        }
    }

    public static StashServerFacade getInstance() {
        return instance;
    }

    public List<PullRequest> getPullRequests() {
        try {
            String pullRequests = stashSession.getPullRequests(PROJECT_KEY, REPO);
            return Lists.transform(getValues(pullRequests), new Function<String, PullRequest>() {
                public PullRequest apply(String s) {
                    return gson.fromJson(s, PullRequestBean.class);
                }
            });
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public List<Comment> getCommentsForCurrentPR(final String path) {

        if (currentPullRequest.isPresent()) {
            try {
                String comments = stashSession.getComments(PROJECT_KEY, REPO, currentPullRequest.get().getId().toString(), path);
                List<Comment> allComments = Lists.transform(getValues(comments), new Function<String, Comment>() {
                    public Comment apply(String s) {
                        return gson.fromJson(s, CommentBean.class);
                    }
                });

                return new ArrayList<Comment>(Collections2.filter(allComments, new Predicate<Comment>() {
                    public boolean apply(Comment comment) {
                        return comment.getAnchor().getPath().equals(path);
                    }
                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    public void addComment(Comment comment) {
        if (currentPullRequest.isPresent()) {
            try {
                stashSession.postComment(PROJECT_KEY, REPO, currentPullRequest.get().getId().toString(), comment);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Optional<PullRequest> getCurrentPullRequest() {
        return currentPullRequest;
    }

    public void setCurrentPullRequest(PullRequest pr) {
        currentPullRequest = Optional.fromNullable(pr);
    }

    public List<String> getChangedFiles() {
        try {
            String changedFiles = stashSession.getChangedFiles(PROJECT_KEY, REPO, currentPullRequest.get().getId().toString());
            return Lists.transform(getValues(changedFiles), new Function<String, String>() {
                public String apply(String s) {
                    return gson.fromJson(s, ChangeBean.class).getFilePath();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private List<String> getValues(String json) {
        List<String> stringValues = new ArrayList<String>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray values = jsonObject.getJSONArray("values");

            for (int i = 0; i < values.length(); i++) {
                String objectString = values.getJSONObject(i).toString();
                stringValues.add(objectString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stringValues;
    }

    public void testServerConnection(ConnectionCfg httpConnectionCfg) throws RemoteApiException {

    }

    public ServerType getServerType() {
        return ServerType.STASH_SERVER;
    }
}
