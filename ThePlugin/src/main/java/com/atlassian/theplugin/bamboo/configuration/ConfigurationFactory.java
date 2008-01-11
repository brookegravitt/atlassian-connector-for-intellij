package com.atlassian.theplugin.bamboo.configuration;

/**
 * Provide an instance of Bamboo BambooConfiguration.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:58:42 AM
 */
public abstract class ConfigurationFactory {
    private static BambooConfiguration bambooConfiguration;

    public static BambooConfiguration getConfiguration() {
        return bambooConfiguration;
    }

    public static void setConfiguration(BambooConfiguration newConfiguration) {
        bambooConfiguration = newConfiguration;
    }
}
