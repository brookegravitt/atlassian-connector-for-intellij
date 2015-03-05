/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.theplugin.idea.jira.logtime;

import com.atlassian.theplugin.idea.jira.LogTimeCheckinHandler;
import com.intellij.openapi.vcs.CheckinProjectPanel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LogTimeInvocationHandler implements InvocationHandler {
    final private LogTimeCheckinHandler logTimeCheckinHandler;

    public LogTimeInvocationHandler(LogTimeCheckinHandler logTimeCheckinHandlerFactory) {
        this.logTimeCheckinHandler = logTimeCheckinHandlerFactory;
    }

    public Object invoke(Object o, Method method, Object[] params) throws Throwable {

        if ("createHandler".equals(method.getName())) {
            CheckinProjectPanel checkinProjectPanel = (CheckinProjectPanel) params[0];
            return logTimeCheckinHandler.createHandler(checkinProjectPanel);

        }
        return null;
    }

}
