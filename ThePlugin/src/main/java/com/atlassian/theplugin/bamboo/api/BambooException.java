package com.atlassian.theplugin.bamboo.api;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 14:31:38
 * To change this template use File | Settings | File Templates.
 */
public class BambooException extends Exception{

    public BambooException(String message) {
        super(message);
    }

    public BambooException(String message, Throwable throwable) {
        super(message, throwable);
    }
}