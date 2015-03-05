package com.atlassian.connector.intellij.stash;

import com.atlassian.theplugin.commons.remoteapi.ProductSession;

import java.io.IOException;

public interface StashSession extends ProductSession {
    String getPullRequests(String project, String repo) throws IOException;
    String getComments(String projectKey, String repo, String pullRequestId, String path) throws IOException;
    void postComment(String projectKey, String repo, String pullRequestId, Comment comment) throws IOException;
}
