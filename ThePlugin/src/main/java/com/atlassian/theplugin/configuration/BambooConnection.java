package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.api.bamboo.RestApi;
import com.atlassian.theplugin.api.bamboo.BambooLoginException;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-14
 * Time: 11:10:07
 * To change this template use File | Settings | File Templates.
 */
public class BambooConnection {


    public void connect(String serverUrl, String userName, String password) throws ConnectionException {

        try {
            RestApi apiHandler = RestApi.login(serverUrl, userName, password);
        } catch (BambooLoginException e) {
            throw new ConnectionException("Connection to Bamboo failed: " + e.getMessage());
        }

    }
}
