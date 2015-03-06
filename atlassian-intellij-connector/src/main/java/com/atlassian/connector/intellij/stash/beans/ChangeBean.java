package com.atlassian.connector.intellij.stash.beans;

import com.atlassian.connector.intellij.stash.Change;

public class ChangeBean  implements Change {
    private Path path;
    private String type;

    public String getFilePath()
    {
        return path.toString;
    }

    public String getChangeType()
    {
        return type;
    }

    private static class Path
    {
        String toString;
    }
}
