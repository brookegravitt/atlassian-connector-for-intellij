package com.atlassian.theplugin.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 3:13:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class StoopidActionClass extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Application application =
                ApplicationManager.getApplication();
        ThePluginApplicationComponent component =
                application.getComponent(
                        ThePluginApplicationComponent.class);
        component.sayHello();
    }
}
