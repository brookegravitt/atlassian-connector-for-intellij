package com.atlassian.theplugin.api.bamboo;

import junit.framework.TestCase;
import com.atlassian.theplugin.api.bamboo.BambooLoginException;
import com.atlassian.theplugin.api.bamboo.RestApi;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 15:08:11
 * To change this template use File | Settings | File Templates.
 */
public class RestApiTest extends TestCase {
    public void testSuccessBambooLogin () throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "user");
        assertNotNull(apiHandler);
    }

    public void testWrongPortBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:80/atlassian-bamboo-1.2.4/", "user", "user");
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongUrlBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bambooXXX/", "user", "user");
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongUserBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "userXXX", "user");
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongPasswordBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "userXXX");
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

}
