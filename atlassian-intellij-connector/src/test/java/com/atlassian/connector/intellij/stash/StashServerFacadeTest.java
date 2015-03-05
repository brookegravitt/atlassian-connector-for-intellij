package com.atlassian.connector.intellij.stash;

import com.atlassian.connector.intellij.stash.beans.AnchorBean;
import com.atlassian.connector.intellij.stash.beans.CommentBean;
import com.atlassian.connector.intellij.stash.impl.StashServerFacadeImpl;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class StashServerFacadeTest {

    private StashServerFacade facade = new StashServerFacadeImpl();
    @Test
    public void testGetPullRequests() throws Exception {
        List<PullRequest> pullRequests = facade.getPullRequests();
        assertEquals("no milk", pullRequests.get(0).getTitle());
        assertEquals("Bartlomiej Lewandowski", pullRequests.get(0).getAuthor().getName());
    }

    @Test
    public void testGetComments() throws Exception {
        List<PullRequest> pullRequests = facade.getPullRequests();
        PullRequest request = pullRequests.get(0);
        facade.setCurrentPullRequest(request);
        List<Comment> comments = facade.getCommentsForCurrentPR("readme.txt");
        assertEquals("Leo, why?", comments.get(0).getText());
    }

    @Test
    public void testAddComment() throws Exception {
        List<PullRequest> pullRequests = facade.getPullRequests();
        PullRequest request = pullRequests.get(0);
        facade.setCurrentPullRequest(request);

        facade.addComment(new CommentBean("comment from facade", new AnchorBean(3, "test.java")));
    }

    @Test
    public void testGetChangedFiles() throws Exception {
        List<PullRequest> pullRequests = facade.getPullRequests();
        PullRequest request = pullRequests.get(0);
        facade.setCurrentPullRequest(request);

        List<String> changedFiles = facade.getChangedFiles();
        assertFalse(changedFiles.isEmpty());
    }
}
