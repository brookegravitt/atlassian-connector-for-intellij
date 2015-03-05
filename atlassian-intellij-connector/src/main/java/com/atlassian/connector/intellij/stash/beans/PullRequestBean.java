package com.atlassian.connector.intellij.stash.beans;

import com.atlassian.connector.intellij.stash.Author;
import com.atlassian.connector.intellij.stash.PullRequest;


public class PullRequestBean implements PullRequest{
    private String title;
    private AuthorBean author;
    private Long id;
    private GitRef fromRef;

    public String getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    public Long getId() {
        return id;
    }

    public String getRef() {
        return fromRef.id.replaceAll("refs/heads/", "");
    }

    @Override
    public String toString() {
        return title + " -> " + fromRef.id + " (by " + author.getName() + ")";
    }

    private static class GitRef
    {
        String id;
    }
}
