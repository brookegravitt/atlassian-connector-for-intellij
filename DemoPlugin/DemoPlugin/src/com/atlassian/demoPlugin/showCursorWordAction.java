package com.atlassian.demoPlugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-09
 * Time: 14:29:25
 * To change this template use File | Settings | File Templates.
 */
public class showCursorWordAction extends AnAction {

    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(e.getDataContext().getData(DataKeys.EDITOR.getName()) != null);
    }

    public void actionPerformed(AnActionEvent e) {

        Application application = ApplicationManager.getApplication();

        DemoPluginApplicationComponent component = application.getComponent(DemoPluginApplicationComponent.class);

        component.sayHello();


    }

 
}
