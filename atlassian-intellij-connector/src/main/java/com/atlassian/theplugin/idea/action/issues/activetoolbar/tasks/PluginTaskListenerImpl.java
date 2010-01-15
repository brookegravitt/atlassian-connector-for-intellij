package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

/**
 * @author pmaruszak
 * @date Jan 13, 2010
 */
public class PluginTaskListenerImpl implements PluginTaskListener {
    private final PluginTaskManager manager;

    public PluginTaskListenerImpl(PluginTaskManager manager) {
        this.manager = manager;
    }

    public void taskActivated(Object localTask) {

    }
}
