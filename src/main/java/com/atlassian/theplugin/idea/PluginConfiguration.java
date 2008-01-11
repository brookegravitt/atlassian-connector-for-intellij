package com.atlassian.theplugin.idea;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:16:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginConfiguration {
    private BambooConfiguration bambooConfiguration = new BambooConfiguration();


    public BambooConfiguration getBambooConfiguration() {
        return bambooConfiguration;
    }

    public void setBambooConfiguration(BambooConfiguration newConfiguration) {
        bambooConfiguration = newConfiguration;

    }
}