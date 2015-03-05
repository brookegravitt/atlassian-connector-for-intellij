package com.atlassian.connector.intellij.stash;

import com.atlassian.connector.intellij.stash.beans.UserBean;

/**
 * Created by klopacinski on 2015-03-05.
 */
public interface Comment {

    public String getText();

    public UserBean getAuthor();

    public Anchor getAnchor();

    public void setText(String text);

    public void setAuthor(UserBean author);

    public void setAnchor(Anchor anchor);
}
