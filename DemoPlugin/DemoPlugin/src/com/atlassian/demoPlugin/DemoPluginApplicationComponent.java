package com.atlassian.demoPlugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-10
 * Time: 16:56:49
 * To change this template use File | Settings | File Templates.
 */
public class DemoPluginApplicationComponent implements ApplicationComponent {
    public DemoPluginApplicationComponent() {
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "com.atlassian.demoPlugin.DemoPluginApplicationComponent";
    }

    public void sayHello() {
        Messages.showMessageDialog("w dupe jeza", "gowniane okienko", Messages.getInformationIcon());
    }
}
