package com.atlassian.theplugin.jira;

public final class JIRAServerFactory {
    private static JIRAServerFacade facade = new JIRAServerFacadeImpl();

    private JIRAServerFactory() {
    }

    public static JIRAServerFacade getJIRAServerFacade() {
        return facade;
    }
}