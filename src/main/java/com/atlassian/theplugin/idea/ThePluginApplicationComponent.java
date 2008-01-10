package com.atlassian.theplugin.idea;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 2:57:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThePluginApplicationComponent implements ApplicationComponent, Configurable {
    private String phrase = "Compiled Hardcode";
    private PluginConfigurationForm form;
    private PluginConfiguration configuration = new PluginConfiguration();


    @Nls
    public String getDisplayName() {
        return "The Plugin";
    }

    @Nullable
    public Icon getIcon() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "ThePluginApplicationComponent";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void initComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    void sayHello() {
        // Show dialog with message
        Messages.showMessageDialog(
                phrase,
                "Sample",
                Messages.getInformationIcon()
        );
    }

    public JComponent createComponent() {
        if (form == null) {
            form = new PluginConfigurationForm();
        }
        return form.getRootComponent();

    }

    public boolean isModified() {
        return form != null && form.isModified(configuration);
    }

    public void apply() throws ConfigurationException {
        if (form != null) {
            // Get data from form to component
            form.getData(configuration);
        }

    }

    public void reset() {
        if (form != null) {
            // Reset form data from component
            form.setData(configuration);
        }
    }

    public void disposeUIResources() {
        form = null;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(final String phrase) {
        this.phrase = phrase;
    }
}

