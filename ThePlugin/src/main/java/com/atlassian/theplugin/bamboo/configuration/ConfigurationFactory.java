package com.atlassian.theplugin.bamboo.configuration;

/**
 * Provide an instance of Bamboo Configuration.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:58:42 AM
 */
public abstract class ConfigurationFactory {
    private static Configuration configuration;

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static void setConfiguration(Configuration newConfiguration) {
        configuration = newConfiguration;
    }
}
