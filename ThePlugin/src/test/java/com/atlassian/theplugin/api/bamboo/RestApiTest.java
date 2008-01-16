package com.atlassian.theplugin.api.bamboo;

import junit.framework.TestCase;
import com.atlassian.theplugin.api.bamboo.BambooLoginException;
import com.atlassian.theplugin.api.bamboo.RestApi;
import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooBuildInfo;

import java.util.List;

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

    public void testWrongParamsBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login("", "", "");
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testProjectList() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "user");
        List<BambooProject> projects = apiHandler.listProjectNames();
        assertFalse(projects.size() == 0);
    }

    public void testPlanList() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "user");
        List<BambooPlan> plans = apiHandler.listPlanNames();
        assertFalse(plans.size() == 0);
    }

    public void testBuildForPlan() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "user");
        BambooBuildInfo build = apiHandler.getLatestPlanBuild("TP-DEF");
        assertNotNull(build);
    }

    public void testRecentBuilds() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "user");
        List builds = apiHandler.getLatestProjectBuilds("TP");
        assertNotNull(builds);
    }

}
