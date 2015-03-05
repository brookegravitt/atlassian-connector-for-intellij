package com.atlassian.connector.intellij.stash.beans;

import com.atlassian.connector.intellij.stash.Author;
import com.atlassian.connector.intellij.stash.PullRequest;


public class PullRequestBean implements PullRequest{
    private String title;
    private AuthorBean author;

    public String getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return title;
    }
}
