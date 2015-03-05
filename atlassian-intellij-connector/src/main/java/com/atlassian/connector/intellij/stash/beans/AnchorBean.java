package com.atlassian.connector.intellij.stash.beans;

import com.atlassian.connector.intellij.stash.Anchor;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class AnchorBean implements Anchor {
    private int line;
    private String path;

    public int getLine() {
        return line;
    }

    public void setLine(int i) {
        this.line = line;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
