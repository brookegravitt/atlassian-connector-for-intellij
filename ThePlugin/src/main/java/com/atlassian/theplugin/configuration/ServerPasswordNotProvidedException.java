package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jan 23, 2008
 * Time: 5:08:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerPasswordNotProvidedException extends Exception {
    public ServerPasswordNotProvidedException(String message) {
        super(message);
    }
}
