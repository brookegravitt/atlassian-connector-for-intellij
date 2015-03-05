package com.atlassian.connector.intellij.stash;

import java.util.List;

/**
 * Created by klopacinski on 2015-03-05.
 */
public interface StashServerFacade {

    public List<PullRequest> getPullRequests();

    public List<Comment> getComments(PullRequest pr, String path);
}
