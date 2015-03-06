package com.atlassian.connector.intellij.stash;

public interface Change {

    String getFilePath();

    String getChangeType();
}
