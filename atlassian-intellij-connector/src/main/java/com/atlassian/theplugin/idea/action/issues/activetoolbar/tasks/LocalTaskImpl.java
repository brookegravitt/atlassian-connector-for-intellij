package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.util.PluginUtil;

import java.lang.reflect.Method;
import java.util.List;

/**
 * User: pmaruszak
 */


public class LocalTaskImpl implements LocalTask {
    public static final String LOCAL_TASK_IMPL_CLASS = "com.intellij.tasks.impl.LocalTaskImpl";


    private static final String CANNOT_GET_LOCAL_TASK_ID = "Cannot get local task id";
    private static final String CANNOT_GET_LOCAL_TASK_SUMMARY = "Cannot get local task summary";
    private static final String CANNOT_GET_LOCAL_TASK_ASSOCIATED_CHANGE_LIST = "Cannot get local task associated change list";

    private final Object localTaskObj;
    private Class localTaskImplClass = null;

    public LocalTaskImpl(Object localTask, ClassLoader classLoader) {
        this.localTaskObj = localTask;        
        try {
            localTaskImplClass = classLoader.loadClass(LOCAL_TASK_IMPL_CLASS);
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error(CANNOT_GET_LOCAL_TASK_ID, e);
        }
    }

    public String getId() {

        if (localTaskImplClass != null) {
            try {
                Method getTaskId = localTaskImplClass.getMethod("getId");
                Object localObj = getTaskId.invoke(this.localTaskObj);
                return localObj.toString();
            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_GET_LOCAL_TASK_ID, e);
            }
        }

        return null;
    }

    public String getIssueUrl() {
        if (localTaskImplClass != null) {
            try {
                Method getUrlMethod = localTaskImplClass.getMethod("getIssueUrl");
                Object issueUrlObj = getUrlMethod.invoke(localTaskObj);
                return issueUrlObj.toString();
            } catch (Exception e) {
                PluginUtil.getLogger().error(CANNOT_GET_LOCAL_TASK_SUMMARY, e);
            }
        }
        return null;
    }

    public Object getLocalTaskObj() {
        return localTaskObj;
    }


    public List<Object> getChangeLists() {
        Method getChangeListsMethod = null;
        List taskChangeLists = null;
        try {
            getChangeListsMethod = localTaskImplClass.getMethod("getChangeLists");
            taskChangeLists = (List) getChangeListsMethod.invoke(localTaskObj);
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot get Change Lists from task", e);
        }

        return taskChangeLists;
    }


}
