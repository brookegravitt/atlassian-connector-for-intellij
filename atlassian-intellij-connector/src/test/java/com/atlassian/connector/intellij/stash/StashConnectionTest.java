package com.atlassian.connector.intellij.stash;

import com.atlassian.connector.intellij.stash.impl.StashRestSession;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class StashConnectionTest {
    @Test
    public void testCanGetPullRequests() throws Exception {
        StashRestSession stashRestSession = new StashRestSession();
        stashRestSession.login("blewandowski", "blewandowski".toCharArray());
        String pullRequests = stashRestSession.getPullRequests("GM", "gitmilk");
        assertFalse(pullRequests.isEmpty());
    }
}
