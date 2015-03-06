package com.atlassian.connector.intellij.stash.beans;

import com.atlassian.connector.intellij.stash.Anchor;
import com.atlassian.connector.intellij.stash.Comment;

public class CommentBean implements Comment{

    private String text;
    private UserBean author;
    private AnchorBean anchor;

    public CommentBean() {}

    public CommentBean(String text, Anchor anchor)
    {
        this.text = text;
        this.anchor = (AnchorBean)anchor;
    }

    public String getText() {
        return text;
    }

    public UserBean getAuthor() {
        return author;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAuthor(UserBean author) {
        this.author = (UserBean)author;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = (AnchorBean)anchor;
    }
}
