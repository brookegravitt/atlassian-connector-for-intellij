package com.atlassian.connector.intellij.stash;

import java.util.Date;

/**
 * Created by klopacinski on 2015-03-05.
 */
public interface PullRequest {
    String getTitle();
    Author getAuthor();
    Long getId();
    String getRef();

    String getState();

    String getDescription();

    Date getCreationDate();
}
