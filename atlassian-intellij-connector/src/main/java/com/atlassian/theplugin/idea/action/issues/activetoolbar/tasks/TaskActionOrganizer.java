package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * @author pmaruszak
 * @date: Feb 2, 2010
 */
public final class TaskActionOrganizer {
    private static boolean isInitialized = false;
    private TaskActionOrganizer() {
    }

    public static void organizeTaskActionsInToolbar() {

            DefaultActionGroup tasksGroup = getTaskActionGroup();
            if (tasksGroup != null && !isInitialized/*&&  (ActionManagerImpl.getInstance().getAction("ThePlugin.TasksToolbar")) != null*/) {
                DefaultActionGroup pluginTaskActions =
                        (DefaultActionGroup) ActionManager.getInstance().getAction("ThePlugin.TasksToolbar");
                if (pluginTaskActions != null) {
                    tasksGroup.add(pluginTaskActions);
                    isInitialized = true;
                    removePluginTaskCombo();
                }
            }
        }

       /**
       * Because TasksToolbar DefaultActionGroup has no name. We search for specified class in DefaultActionGroup
       * from MainToolbar
       *
       * @return DefaultActionGroup
       */
      @Nullable
      private static DefaultActionGroup getTaskActionGroup() {
          DefaultActionGroup mainToolBar = (DefaultActionGroup) (ActionManagerImpl.getInstance().getAction("MainToolBar"));
          if (mainToolBar != null) {
              for (AnAction action : getGroupActionsOrStubs(mainToolBar)) {
                  if (action instanceof DefaultActionGroup) {
                      DefaultActionGroup group = (DefaultActionGroup) action;
                      for (AnAction groupAction : getGroupActionsOrStubs(group)) {
                          if (groupAction.getClass().getName().equals("com.intellij.tasks.actions.SwitchTaskCombo")) {
                              return group;
                          }
                      }
                  }
              }
          }

          return null;
      }

       private static AnAction[] getGroupActionsOrStubs(DefaultActionGroup group) {
          AnAction[] actions = new AnAction[0];
          ClassLoader classLoader = DefaultActionGroup.class.getClassLoader();

          if (classLoader != null) {
              actions = new AnAction[group.getChildrenCount()];
              try {
                  Method getChildActionsOrStubsMethod = DefaultActionGroup.class.getMethod("getChildActionsOrStubs");
                  actions = (AnAction[]) getChildActionsOrStubsMethod.invoke(group);
              } catch (Exception e) {
                  PluginUtil.getLogger().error("Cannot get AnAction[] for group " + group.getTemplatePresentation().getText());
              }
          }

          return actions;
      }

      private static void removePluginTaskCombo() {
        DefaultActionGroup pluginTaskActions =
                (DefaultActionGroup) ActionManager.getInstance().getAction("ThePlugin.ActiveToolbar");
        DefaultActionGroup mainToolBar = (DefaultActionGroup) (ActionManagerImpl.getInstance().getAction("MainToolBar"));

        if (mainToolBar != null && pluginTaskActions != null) {
            mainToolBar.remove(pluginTaskActions);
        }
    }

    @NotNull
    public String getComponentName() {
        return TaskActionOrganizer.class.getName();
    }

    public void initComponent() {
        organizeTaskActionsInToolbar();
    }

    public void disposeComponent() {

    }
}
