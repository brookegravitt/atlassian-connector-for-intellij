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

    @Override
    public String toString() {
        return path != null ? path.toString() : "";
    }

    private static class Path
    {
        String toString;

        @Override
        public String toString() {
            return toString;
        }
    }
}
