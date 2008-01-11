package com.atlassian.theplugin.configuration;

/**
 * Bamboo server configuration.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 12:04:40 PM
 */
public interface Server {
    String getName();
    String getUrlString();
    String getUsername();
    String getPassword();
}
