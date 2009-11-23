package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Nov 20, 2009
 * Time: 1:09:50 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TaskRepositoryType {
    String getName();
    List<TaskRepository> getRepositories();

    @Nullable
    TaskRepository createRepository();

    Object getRepositoryTypeObj();

    Class getTaskRepositoryTypeClass();
}
