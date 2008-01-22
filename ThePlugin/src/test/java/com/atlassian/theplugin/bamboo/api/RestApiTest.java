package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooProject;
import junit.framework.TestCase;

import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 15:08:11
 * To change this template use File | Settings | File Templates.
 */
public class RestApiTest extends TestCase {
    private static final String SERVER_URL = "http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4";
    private static final String USER_NAME = "user";
    private static final String PASSWORD = "d0n0tch@nge";


    public void testSuccessBambooLogin () throws Exception {
        RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD);
        assertNotNull(apiHandler);
        apiHandler.logout();
    }

    public void testSuccessBambooLoginURLWithSlash () throws Exception {
        RestApi apiHandler = RestApi.login(SERVER_URL + "/", USER_NAME, PASSWORD);
        assertNotNull(apiHandler);
        apiHandler.logout();
    }

    public void testNullParamsLogin() throws Exception {
        try {
            RestApi apiHandler = RestApi.login(null, null, null);
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongPortBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login(SERVER_URL.replaceAll("8080", "9090"), USER_NAME, PASSWORD);
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongUrlBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login(SERVER_URL.replaceAll("bamboo", "xxx"), "user", "d0n0tch@nge");
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongUserBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME + "xxx", PASSWORD);
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongPasswordBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD + "xxx");
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testNoPasswordBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, "");
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
        RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD);
        List<BambooProject> projects = apiHandler.listProjectNames();
        assertFalse(projects.size() == 0);
        apiHandler.logout();
    }

    public void testPlanList() throws Exception {
        RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD);
        List<BambooPlan> plans = apiHandler.listPlanNames();
        assertFalse(plans.size() == 0);
        apiHandler.logout();
    }

    public void testBuildForPlan() throws Exception {
        RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD);
        BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
        assertNotNull(build);
        apiHandler.logout();
    }

    public void testBuildForNonExistingPlan() throws Exception {
        RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD);
        try {
            BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF-NON-EXISTING");
            fail();
        } catch (BambooException e) {
        }
        apiHandler.logout();
    }

    public void testBuildForEmptyPlan() throws Exception {
        RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD);
        try {
            BambooBuild build = apiHandler.getLatestBuildForPlan("");
            fail();
        } catch (BambooException e) {
        }
        apiHandler.logout();
    }

    public void testRecentBuilds() throws Exception {
        RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD);
        List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP");
        assertNotNull(builds);
        apiHandler.logout();
    }

    public void testRecentBuildsForNonExistingProject() throws Exception {
        RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD);
        try {
            List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP-NON-EXISTING");
            fail();
        } catch (BambooException e) {

        }
        apiHandler.logout();
    }

    public void testRecentBuildsForEmptyProject() throws Exception {
        RestApi apiHandler = RestApi.login(SERVER_URL, USER_NAME, PASSWORD);
        try {
            List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP-NON-EXISTING");
            fail();
        } catch (BambooException e) {

        }
        apiHandler.logout();
    }

    public void testUrlEncodingBambooPassword () throws Exception {
        try {
            RestApi apiHandler = RestApi.login(SERVER_URL, "", PASSWORD + "&username=" + USER_NAME);
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }
}
