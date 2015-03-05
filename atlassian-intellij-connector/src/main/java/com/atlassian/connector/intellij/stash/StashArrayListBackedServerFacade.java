package com.atlassian.connector.intellij.stash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class StashArrayListBackedServerFacade implements StashServerFacade {

    private static StashArrayListBackedServerFacade instance;

    static {
        instance = new StashArrayListBackedServerFacade();
    }

    private HashMap<String, List<Comment>> pathToCommentsMap = new HashMap<String, List<Comment>>();

    public StashArrayListBackedServerFacade() {
        fillInitialData();
    }

    public static StashArrayListBackedServerFacade getInstance() {
        return instance;
    }

    public List<PullRequest> getPullRequests() {
        return null;
    }

    public List<Comment> getComments(PullRequest pr, String path) {
        return pathToCommentsMap.get(path);
    }

    private void fillInitialData() {
        SimpleAuthor author = new SimpleAuthor();
        author.setName("Zbigniew");

        SimpleAnchor anchor = new SimpleAnchor();
        anchor.setLine(2);

        SimpleComment comment = new SimpleComment();
        comment.setAuthor(author);
        comment.setText("Very insightful comment.");
        comment.setAnchor(anchor);

        ArrayList<Comment> comments = new ArrayList<Comment>();
        comments.add(comment);

        pathToCommentsMap.put("readme.txt", comments);
    }
}
