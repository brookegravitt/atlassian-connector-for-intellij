package com.atlassian.connector.intellij.stash;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class StashArrayListBackedServerFacade implements StashServerFacade {

    private ArrayList<Comment> comments = new ArrayList<Comment>();

    public StashArrayListBackedServerFacade() {

    }

    public List<PullRequest> getPullRequests() {
        return null;
    }

    public List<Comment> getComments(String path) {
        return null;
    }

    private void fillInitialData() {
        
    }
}
