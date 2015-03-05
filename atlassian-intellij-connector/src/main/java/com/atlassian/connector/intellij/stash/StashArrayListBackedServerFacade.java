package com.atlassian.connector.intellij.stash;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;

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

    private ArrayListMultimap<String, Comment> pathToCommentsMap = ArrayListMultimap.create();

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

    public void addComment(Comment comment) {
        String path = comment.getAnchor().getPath();

        pathToCommentsMap.put(path, comment);
    }

    private void fillInitialData() {
        SimpleAuthor author = new SimpleAuthor();
        author.setName("Zbigniew");

        SimpleAnchor anchor = new SimpleAnchor();
        anchor.setLine(2);

        SimpleComment comment = new SimpleComment();
//        comment.setAuthor(author);
        comment.setText("Very insightful comment.");
        comment.setAnchor(anchor);

        pathToCommentsMap.put("readme.txt", comment);
    }

    public void testServerConnection(ConnectionCfg httpConnectionCfg) throws RemoteApiException {

    }

    public ServerType getServerType() {
        return null;
    }
}
