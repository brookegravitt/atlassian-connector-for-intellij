package com.atlassian.demoPlugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-10
 * Time: 16:56:49
 * To change this template use File | Settings | File Templates.
 */
public class DemoPluginApplicationComponent implements ApplicationComponent, Configurable {
    private String userName;
    private DemoPuginConfigurationForm form;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    @Nls
    public String getDisplayName() {
        return "Demo Plugin";
    }

    @Nullable
    public Icon getIcon() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return "Test Help";
    }

    public JComponent createComponent() {
        if (form == null) {
            form = new DemoPuginConfigurationForm();
        }

        return form.getRootComponent();
    }

    public boolean isModified() {
        return form != null && form.isModified(this);
    }

    public void apply() throws ConfigurationException {
        if (form != null) {
            form.getData(this);
        }
    }

    public void reset() {
        if (form != null) {
            form.setData(this);
        }
    }

    public void disposeUIResources() {
        form = null;
    }
}
