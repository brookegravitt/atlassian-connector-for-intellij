package com.atlassian.connector.intellij.stash;

import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.google.common.base.Optional;

import java.util.List;

/**
 * Created by klopacinski on 2015-03-05.
 */
public interface StashServerFacade extends ProductServerFacade{

    public List<PullRequest> getPullRequests();

    public List<Comment> getCommentsForCurrentPR(String path);

    public void addComment(Comment comment);

    public Optional<PullRequest> getCurrentPullRequest();

    public void setCurrentPullRequest(PullRequest pr);

    public List<String> getChangedFiles();
}
