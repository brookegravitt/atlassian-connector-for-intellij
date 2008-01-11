package com.atlassian.theplugin.api.bamboo;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 14:31:38
 * To change this template use File | Settings | File Templates.
 */
public class BambooLoginException extends Exception{

    public BambooLoginException(String message) {
        super(message);
    }
    
    public BambooLoginException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
