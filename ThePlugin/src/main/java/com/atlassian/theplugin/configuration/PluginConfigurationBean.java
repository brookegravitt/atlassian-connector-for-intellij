package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:16:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginConfigurationBean implements PluginConfiguration {
    private BambooConfigurationBean bambooConfiguration = new BambooConfigurationBean();


    /**
     * For storage purposes.
     *
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public BambooConfigurationBean getBambooConfigurationData() {
        return bambooConfiguration;
    }

    /**
     * For storage purposes.
     *
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public void setBambooConfigurationData(BambooConfigurationBean newConfiguration) {
        bambooConfiguration = newConfiguration;

    }
    /**
     * Implemnentation for the interface.
     *
     * Do not mistake for #getBambooConfigurationData()
     */
    public BambooConfiguration getBambooConfiguration() {
        return bambooConfiguration;
    }
}