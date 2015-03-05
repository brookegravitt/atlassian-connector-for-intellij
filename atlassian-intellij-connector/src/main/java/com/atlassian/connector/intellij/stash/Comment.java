package com.atlassian.connector.intellij.stash;

/**
 * Created by klopacinski on 2015-03-05.
 */
public interface Comment {

    public String getText();

    public Author getAuthor();

    public void setText(String text);

    public void setAuthor(Author author);
}
