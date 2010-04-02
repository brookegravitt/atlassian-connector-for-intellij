package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.lang.reflect.Method;

/**
 * @author pmaruszak
 * @date Feb 2, 2010
 */
public final class PluginTaskManagerFacade {
    private PluginTaskManagerFacade() {
    }

    public static void activateIssue(final Project project, final ActiveJiraIssue issue) {
        if (!isValidIdeaVersion()) {
            return;
        }

        try {
            final Class ptmClass = Class.forName("com.atlassian.connector.intellij.tasks.PluginTaskManager");
            final Method activateIssueMethod = ptmClass.getMethod("activateIssue", ActiveJiraIssue.class);
            final Object ptmObj = project.getPicoContainer().getComponentInstanceOfType(ptmClass);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    try {
                        activateIssueMethod.invoke(ptmObj, issue);
                    } catch (Exception e) {
                        PluginUtil.getLogger().error("Cannot activate issue.", e);
                    }
                }
            });


        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot activate issue.", e);
        }

    }

    public static void silentActivateIssue(final Project project, final ActiveJiraIssue issue) {
        if (!isValidIdeaVersion()) {
            return;
        }
        try {
            final Class ptmClass = Class.forName("com.atlassian.connector.intellij.tasks.PluginTaskManager");
            final Method silentActivateIssue = ptmClass.getMethod("silentActivateIssue", ActiveJiraIssue.class);
            final Object ptmObj = project.getPicoContainer().getComponentInstanceOfType(ptmClass);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        silentActivateIssue.invoke(ptmObj, issue);
                    } catch (Exception e) {
                        PluginUtil.getLogger().error("Cannot silent activate issue.", e);
                    }
                }
            });
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot silent activate issue.", e);
        }

    }

    public static boolean isValidIdeaVersion() {
        return IdeaVersionFacade.getInstance().isIdea9()
                && !IdeaVersionFacade.getInstance().isCommunityEdition()
                && isTaskPluginEnabled();
    }

    private static boolean isTaskPluginEnabled() {
        IdeaPluginDescriptor descriptor = getTaskManagerDescriptor();
        return descriptor != null && ((IdeaPluginDescriptorImpl) descriptor).isEnabled();
    }

    public static IdeaPluginDescriptor getTaskManagerDescriptor() {
        for (IdeaPluginDescriptor descriptor : ApplicationManager.getApplication().getPlugins()) {
            if (descriptor.getPluginId().getIdString().equals("com.intellij.tasks")) {
                return descriptor;
            }
        }
        return null;
    }

    public static void deactivateToDefaultTask(final Project project) {
        if (!isValidIdeaVersion()) {
            return;
        }
        try {

            final Class ptmClass = Class.forName("com.atlassian.connector.intellij.tasks.PluginTaskManager");
            final Method deactivateToDefaultTaskMethod = ptmClass.getMethod("deactivateToDefaultTask");
            final Object ptmObj = project.getPicoContainer().getComponentInstanceOfType(ptmClass);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        deactivateToDefaultTaskMethod.invoke(ptmObj);
                    } catch (Exception e) {
                        PluginUtil.getLogger().error("Cannot deactivate issue to default task.", e);
                    }
                }
            });

        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot deactivate issue to default task.", e);
        }
    }

    public static void silentDeactivateIssue(final Project project) {
        if (!isValidIdeaVersion()) {
            return;
        }
        try {

            final Class ptmClass = Class.forName("com.atlassian.connector.intellij.tasks.PluginTaskManager");
            final Method silentDeactivateIssue = ptmClass.getMethod("silentDeactivateIssue");
            final Object ptmObj = project.getPicoContainer().getComponentInstanceOfType(ptmClass);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    try {
                        silentDeactivateIssue.invoke(ptmObj);
                    } catch (Exception e) {
                        PluginUtil.getLogger().error("Cannot deactivate issue to default task.", e);
                    }
                }
            });
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot deactivate issue to default task.", e);
        }
    }
}
