package com.atlassian.connector.intellij.stash.impl;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class StashServerJson {
    public String getPullRequests()
    {
        return getTextFromFile("stashresponses/pullrequests.txt");
    }
    public String getComments() {

        return getTextFromFile("stashresponses/comments.txt");
    }

    private String getTextFromFile(String filename) {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
