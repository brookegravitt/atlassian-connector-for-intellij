package com.atlassian.connector.intellij.stash;

import com.atlassian.connector.intellij.stash.beans.AnchorBean;
import com.atlassian.connector.intellij.stash.beans.CommentBean;
import com.atlassian.connector.intellij.stash.impl.StashRestSession;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class StashConnectionTest {

    private StashRestSession stashRestSession;

    @Before
    public void setUp() throws Exception {
        stashRestSession = new StashRestSession();
        stashRestSession.login("blewandowski", "blewandowski".toCharArray());
    }

    @Test
    public void testCanGetPullRequests() throws Exception {
        String pullRequests = stashRestSession.getPullRequests("GM", "gitmilk");
        assertFalse(pullRequests.isEmpty());
    }

    @Test
    public void testCanGetComments() throws Exception {
        String pullRequests = stashRestSession.getComments("GM", "gitmilk", "1", "readme.txt");
        assertFalse(pullRequests.isEmpty());
    }

    @Test
    public void testCanPostComments() throws Exception {
        Comment comment = new CommentBean("test", new AnchorBean(3, "test.java"));

        stashRestSession.postComment("GM", "gitmilk", "1", comment);
    }

    @Test
    public void testCanGetCommentsTwice() throws Exception {
        String pullRequests = stashRestSession.getComments("GM", "gitmilk", "1", "readme.txt");
        stashRestSession.getComments("GM", "gitmilk", "1", "readme.txt");
        assertFalse(pullRequests.isEmpty());
    }

}
