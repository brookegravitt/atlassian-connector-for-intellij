package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.exception.ThePluginException;

public interface ConnectionTester {
    /**
     * Test a connection. Throws exception on failure, nothing on success.
     *
     * @param username
     * @param password
     * @param serverUrl
     * @throws ThePluginException If the connection failed.
     */
    void testConnection(String username, String password, String serverUrl) throws ThePluginException;
}
