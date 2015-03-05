package com.atlassian.connector.intellij.stash.beans;

public class ChangeBean {
    private Path path;

    public String getFilePath()
    {
        return path.toString;
    }

    private static class Path
    {
        String toString;
    }
}
