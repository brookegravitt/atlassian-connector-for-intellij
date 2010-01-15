package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Nov 20, 2009
 * Time: 12:58:30 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TaskRepository {

    String getUrl();

    void setUrl(String url);

    void setUsername(String userName);

    void setPassword(String password);

    void setShared(boolean shared);

    Object getTaskRepositoryObj();

    @Nullable
    TaskRepositoryType getRepositoryType();
}
