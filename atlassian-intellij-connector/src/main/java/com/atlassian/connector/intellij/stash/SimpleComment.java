package com.atlassian.connector.intellij.stash;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class SimpleComment implements Comment {
    private Author author;
    private String text;
    private Anchor anchor;

    public String getText() {
        return text;
    }

    public Author getAuthor() {
        return author;
    }

    public Anchor getAnchor() {
        return anchor;
    }


    public void setText(String text) {
        this.text = text;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }


}
