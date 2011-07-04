package com.atlassian.theplugin.idea.jira;

/**
 * User: kalamon
 * Date: May 22, 2009
 * Time: 12:18:42 PM
 */
public enum RemainingEstimateUpdateMode {

    AUTO("Automatically adjust remaining"),
    UNCHANGED("Leave remaining unchanged"),
    MANUAL("Change remaining to ");

    private String txt;

    RemainingEstimateUpdateMode(String txt) {
        this.txt = txt;
    }

    public String getText() {
        return txt;
    }
}
