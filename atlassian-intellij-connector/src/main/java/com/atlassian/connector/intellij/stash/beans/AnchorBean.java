package com.atlassian.connector.intellij.stash.beans;

import com.atlassian.connector.intellij.stash.Anchor;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class AnchorBean implements Anchor {
    private int line;
    private String path;
    private String lineType;
    private String fileType;

    public AnchorBean() {}

    public AnchorBean(int line, String path) {
        this.line = line;
        this.path = path;
        this.lineType = "ADDED";
        this.fileType = "TO";
    }

    public int getLine() {
        return line;
    }

    public void setLine(int i) {
        this.line = i;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
