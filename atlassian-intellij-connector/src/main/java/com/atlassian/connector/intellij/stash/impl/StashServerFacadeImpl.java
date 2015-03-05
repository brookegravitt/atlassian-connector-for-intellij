package com.atlassian.connector.intellij.stash.impl;

import com.atlassian.connector.intellij.stash.Comment;
import com.atlassian.connector.intellij.stash.PullRequest;
import com.atlassian.connector.intellij.stash.StashServerFacade;
import com.atlassian.connector.intellij.stash.beans.CommentBean;
import com.atlassian.connector.intellij.stash.beans.PullRequestBean;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StashServerFacadeImpl implements StashServerFacade
{
    private final StashServerJson serverJson = new StashServerJson();
    private static final Gson gson = new Gson();

    public List<PullRequest> getPullRequests() {
        String pullRequests = serverJson.getPullRequests();

        return Lists.transform(getValues(pullRequests), new Function<String, PullRequest>() {
            public PullRequest apply(String s) {
                return gson.fromJson(s, PullRequestBean.class);
            }
        });
    }

    public List<Comment> getComments(PullRequest pr, final String path) {
        String comments = serverJson.getComments();

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
    }

    public void addComment(Comment comment) {

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
}
