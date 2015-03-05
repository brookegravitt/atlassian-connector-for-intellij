package com.atlassian.connector.intellij.stash.impl;

import com.atlassian.connector.intellij.stash.Comment;
import com.atlassian.connector.intellij.stash.PullRequest;
import com.atlassian.connector.intellij.stash.StashServerFacade;
import com.atlassian.connector.intellij.stash.beans.PullRequestBean;
import com.google.common.base.Function;
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

    public List<Comment> getComments(String path) {
        return null;
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
