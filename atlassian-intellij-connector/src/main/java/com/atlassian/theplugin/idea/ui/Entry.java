package com.atlassian.theplugin.idea.ui;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jul 17, 2009
 * Time: 11:29:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class Entry {
    private final String label;
    private boolean error;

    private final String value;

    public Entry(String label, String value) {
        this(label, value, false);
    }

    public Entry(final String label, final String value, boolean error) {
        this.value = value;
        this.label = label;
        this.error = error;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public boolean isError() {
        return error;
    }
}
