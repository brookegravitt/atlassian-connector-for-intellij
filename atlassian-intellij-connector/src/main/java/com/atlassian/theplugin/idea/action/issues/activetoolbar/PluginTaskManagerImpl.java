/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.idea.action.issues.activetoolbar;


import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.tasks.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 */

public class PluginTaskManagerImpl extends TaskManager {
    private static Map<Project, PluginTaskManagerImpl> instances = new HashMap<Project, PluginTaskManagerImpl>();
    private Project project;
    private Object taskManager;
    private IdeaPluginDescriptor pluginDescriptor;

    private PluginTaskManagerImpl(final Project project) {

        this.project = project;
        taskManager = getTaskManager(project);
        pluginDescriptor = getTaskManagerDescriptor(project);
    }

    PluginTaskManagerImpl getInstance(Project project) {
        if (!instances.containsKey(project)) {
            instances.put(project, new PluginTaskManagerImpl(project));
        }

        return instances.get(project);

    }


      private static Object getTaskManager(Project project) {

        IdeaPluginDescriptor descriptor = getTaskManagerDescriptor(project);
        if (descriptor != null) {
            try {
                Class taskManagerClass = descriptor.getPluginClassLoader().loadClass(TaskManager.class.getName());
                Method getManager = taskManagerClass.getMethod("getManager", Project.class);
                Object taskManager = getManager.invoke(null, project);
                return taskManager;
            } catch (ClassNotFoundException e) {
                PluginUtil.getLogger().error("Cannot load class:" + TaskManager.class.getName(), e);
            } catch (InvocationTargetException e) {
                PluginUtil.getLogger().error("Cannot load class:" + TaskManager.class.getName(), e);
            } catch (NoSuchMethodException e) {
                PluginUtil.getLogger().error("Cannot load class:" + TaskManager.class.getName(), e);
            } catch (IllegalAccessException e) {
                PluginUtil.getLogger().error("Cannot load class:" + TaskManager.class.getName(), e);
            }
        }

        return null;
    }

    private static IdeaPluginDescriptor getTaskManagerDescriptor(final Project project) {
        for (IdeaPluginDescriptor descriptor : ApplicationManager.getApplication().getPlugins()) {
            if (descriptor.getPluginId().getIdString().equals("com.intellij.tasks")) {
               return descriptor;
            }
        }
        return null;
    }



    @Override
    public Map<String, Task> getCachedIssues() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Task updateIssue(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LocalTask[] getLocalTasks() {
        return new LocalTask[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LocalTask createLocalTask(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void activateTask(@NotNull Task task, boolean b, boolean b1) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ChangeListInfo> getOpenChangelists(Task task) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public LocalTask getActiveTask() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateIssues() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isVcsEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LocalTask getAssociatedTask(LocalChangeList localChangeList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void associateWithTask(LocalChangeList localChangeList) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void closeTask(LocalTask localTask) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeTask(LocalTask localTask) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TaskRepository[] getAllRepositories() {
        return new TaskRepository[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean testConnection(TaskRepository taskRepository) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TaskRepositoryType[] getAllRepositoryTypes() {
        return new TaskRepositoryType[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T extends TaskRepository> void setRepositories(List<T> ts, TaskRepositoryType<T> tTaskRepositoryType) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
