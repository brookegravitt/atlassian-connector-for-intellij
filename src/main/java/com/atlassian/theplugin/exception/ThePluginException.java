package com.atlassian.theplugin.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-23
 * Time: 12:49:48
 * To change this template use File | Settings | File Templates.
 */
public class ThePluginException extends Exception {

    public ThePluginException(String message) {
        super(message);
    }

    ThePluginException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
