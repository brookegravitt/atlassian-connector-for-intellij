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


    public BambooConfigurationBean getBambooConfiguration() {
        return bambooConfiguration;
    }

    public void setBambooConfiguration(BambooConfigurationBean newConfiguration) {
        bambooConfiguration = newConfiguration;

    }
}