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
public class BambooSessionTest extends TestCase {
    private static final String SERVER_URL = "http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4";
    private static final String SERVER_SSL_URL = "https://lech.atlassian.pl/atlassian-bamboo-1.2.4";    
    private static final String USER_NAME = "user";
    private static final String PASSWORD = "d0n0tch@nge";


    public void testSuccessBambooLogin () throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        assertTrue(apiHandler.isLoggedIn());
        apiHandler.logout();
        assertFalse(apiHandler.isLoggedIn());
    }

    public void testBambooLogout () throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        assertFalse(apiHandler.isLoggedIn());
        apiHandler.logout();
    }

    public void testSuccessBambooLoginURLWithSlash () throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL + "/");
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.logout();
    }

    public void testNullParamsLogin() throws Exception {
        try {
            BambooSession apiHandler = new BambooSession(null);
            apiHandler.login(null, null);
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongUrlBambooLogin () throws Exception {
        try {
            BambooSession apiHandler = new BambooSession(SERVER_URL.replaceAll("bamboo", "xxx"));
            apiHandler.login(USER_NAME, PASSWORD.toCharArray());           
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongUserBambooLogin () throws Exception {
        try {
            BambooSession apiHandler = new BambooSession(SERVER_URL);
            apiHandler.login(USER_NAME + "XXX", PASSWORD.toCharArray());
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongPasswordBambooLogin () throws Exception {
        try {
            BambooSession apiHandler = new BambooSession(SERVER_URL);
            apiHandler.login(USER_NAME, (PASSWORD + "xxx").toCharArray());
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testNoPasswordBambooLogin () throws Exception {
        try {
            BambooSession apiHandler = new BambooSession(SERVER_URL);
            apiHandler.login(USER_NAME, "".toCharArray());
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongParamsBambooLogin () throws Exception {
        try {
            BambooSession apiHandler = new BambooSession("");
            apiHandler.login("", "".toCharArray());
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testProjectList() throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        List<BambooProject> projects = apiHandler.listProjectNames();
        assertFalse(projects.size() == 0);
        apiHandler.logout();
    }

    public void testPlanList() throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        List<BambooPlan> plans = apiHandler.listPlanNames();
        assertFalse(plans.size() == 0);
        apiHandler.logout();
    }

    public void testBuildForPlan() throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
        assertNotNull(build);
        apiHandler.logout();
    }

    public void testBuildForNonExistingPlan() throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        try {
            BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF-NON-EXISTING");
            fail();
        } catch (BambooException e) {
        }
        apiHandler.logout();
    }

    public void testBuildForEmptyPlan() throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        try {
            BambooBuild build = apiHandler.getLatestBuildForPlan("");
            fail();
        } catch (BambooException e) {
        }
        apiHandler.logout();
    }

    public void testRecentBuilds() throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP");
        assertNotNull(builds);
        apiHandler.logout();
    }

    public void testRecentBuildsForNonExistingProject() throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        try {
            List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP-NON-EXISTING");
            fail();
        } catch (BambooException e) {

        }
        apiHandler.logout();
    }

    public void testRecentBuildsForEmptyProject() throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        try {
            List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP-NON-EXISTING");
            fail();
        } catch (BambooException e) {

        }
        apiHandler.logout();
    }

    public void testUrlEncodingBambooPassword () throws Exception {
        try {
            BambooSession apiHandler = new BambooSession(SERVER_URL);
            apiHandler.login("", (PASSWORD + "&username=" + USER_NAME).toCharArray());
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testSuccessBambooLoginOnSSL () throws Exception {
        BambooSession apiHandler = new BambooSession(SERVER_SSL_URL);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.logout();
    }

}
