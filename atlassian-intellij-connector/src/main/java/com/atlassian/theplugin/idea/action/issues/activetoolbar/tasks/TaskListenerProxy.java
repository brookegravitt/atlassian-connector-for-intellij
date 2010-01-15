package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author: pmaruszak
 */
public class TaskListenerProxy implements InvocationHandler {
    public static final String TASK_LISTENER = "com.intellij.tasks.TaskListener";
    
    private static Method taskActivatedMethod;
    private static Class taskListenerInterface;
    private final PluginTaskManager projectTaskManager;

    public static Object newInstance(final  ClassLoader classLoader, final PluginTaskManager pluginTaskManager) {

        try {
            taskListenerInterface = classLoader.loadClass(TASK_LISTENER);
            Object taskListenerObj = java.lang.reflect.Proxy.newProxyInstance(
                    classLoader,
                    new Class[]{taskListenerInterface},
                    new TaskListenerProxy(pluginTaskManager));
            taskActivatedMethod = taskListenerInterface.getMethod("taskActivated",
                    classLoader.loadClass(PluginTaskManager.LOCAL_TASK_CLASS));
            return taskListenerObj;
        } catch (Exception e) {

        }

        return null;
    }


    private TaskListenerProxy(PluginTaskManager projectTaskManager) {

        this.projectTaskManager = projectTaskManager;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
            throws Throwable {
        Object result;
        try {
            if (m.equals(taskActivatedMethod)) {
               System.out.print("task activated");
            }
        } catch (Exception e) {
            throw new RuntimeException("unexpected invocation exception: " +
                    e.getMessage());
        } finally {
            System.out.println("after method " + m.getName());
        }
        return null;
    }
}
