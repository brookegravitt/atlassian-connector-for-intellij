package com.atlassian.connector.intellij.stash;

import com.atlassian.theplugin.commons.remoteapi.ProductSession;

import java.io.IOException;

public interface StashSession extends ProductSession {
    String getPullRequests(String project, String repo) throws IOException;
}
