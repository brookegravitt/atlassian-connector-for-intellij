package com.atlassian.connector.intellij.stash;

import com.atlassian.connector.intellij.stash.impl.StashServerFacadeImpl;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class StashServerFacadeTest {

    private StashServerFacade facade = new StashServerFacadeImpl();
    @Test
    public void testGetPullRequests() throws Exception {
        List<PullRequest> pullRequests = facade.getPullRequests();
        assertEquals("no milk", pullRequests.get(0).getTitle());
        assertEquals("Admin Adminiev", pullRequests.get(0).getAuthor().getName());
    }

    @Test
    public void testGetComments() throws Exception {
        List<PullRequest> pullRequests = facade.getPullRequests();
        PullRequest request = pullRequests.get(0);
        facade.setCurrentPullRequest(request);
        List<Comment> comments = facade.getCommentsForCurrentPR("readme.txt");
        assertEquals("wtf?", comments.get(0).getText());
    }
}
