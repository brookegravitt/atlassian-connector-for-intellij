package com.atlassian.connector.intellij.stash;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class SimpleAnchor implements Anchor {
    private int line;

    public int getLine() {
        return line;
    }

    public void setLine(int i) {
        this.line = line;
    }
}
