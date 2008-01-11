package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.configuration.ConfigurationFactory;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
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

@State(name = "ThePluginSettings", storages = {@Storage(id = "thePlugin", file = "$APP_CONFIG$/thePlugin.xml")})
public class ThePluginApplicationComponent implements ApplicationComponent, Configurable, PersistentStateComponent<PluginConfiguration> {
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
        ConfigurationFactory.setConfiguration(configuration.getBambooConfiguration());
    }

    public void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
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

    public PluginConfiguration getState() {
        return configuration;
    }

    public void loadState(PluginConfiguration state) {
        configuration = state;
    }
}
