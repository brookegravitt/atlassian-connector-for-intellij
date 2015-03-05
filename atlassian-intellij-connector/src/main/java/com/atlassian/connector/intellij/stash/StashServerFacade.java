package com.atlassian.connector.intellij.stash;

import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;

import java.util.List;

/**
 * Created by klopacinski on 2015-03-05.
 */
public interface StashServerFacade extends ProductServerFacade{

    public List<PullRequest> getPullRequests();

    public List<Comment> getComments(PullRequest pr, String path);

    public void addComment(Comment comment);
}
