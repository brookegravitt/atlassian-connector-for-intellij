package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.bamboo.BambooServerFacade;
import com.atlassian.theplugin.bamboo.BambooServerFacadeImpl;

public final class JIRAServerFactory {
    private static JIRAServerFacade facade = new JIRAServerFacadeImpl();

    private JIRAServerFactory() {
    }

    public static JIRAServerFacade getJIRAServerFacade() {
        return facade;
    }
}