package com.atlassian.connector.intellij.stash;

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
}
