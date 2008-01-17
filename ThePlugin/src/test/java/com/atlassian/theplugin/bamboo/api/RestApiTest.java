package com.atlassian.theplugin.bamboo.api;

import junit.framework.TestCase;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.bamboo.api.RestApi;
import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.bamboo.BambooBuild;

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
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
        assertNotNull(apiHandler);
    }

  
    public void testWrongPortBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:90/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongUrlBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bambooXXX/", "user", "d0n0tch@nge");
            fail();
        } catch (BambooLoginException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void testWrongUserBambooLogin () throws Exception {
        try {
            RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "userXXX", "d0n0tch@nge");
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
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
        List<BambooProject> projects = apiHandler.listProjectNames();
        assertFalse(projects.size() == 0);
    }

    public void testPlanList() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
        List<BambooPlan> plans = apiHandler.listPlanNames();
        assertFalse(plans.size() == 0);
    }

    public void testBuildForPlan() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
        BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
        assertNotNull(build);
    }

    public void testBuildForNonExistingPlan() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
        try {
            BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF-NON-EXISTING");
            fail();
        } catch (BambooException e) {
        }
    }

    public void testBuildForEmptyPlan() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
        try {
            BambooBuild build = apiHandler.getLatestBuildForPlan("");
            fail();
        } catch (BambooException e) {
        }
    }

    public void testRecentBuilds() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
        List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP");
        assertNotNull(builds);
    }

    public void testRecentBuildsForNonExistingProject() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
        try {
            List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP-NON-EXISTING");
            fail();
        } catch (BambooException e) {

        }
    }

    public void testRecentBuildsForEmptyProject() throws Exception {
        RestApi apiHandler = RestApi.login("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/", "user", "d0n0tch@nge");
        try {
            List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP-NON-EXISTING");
            fail();
        } catch (BambooException e) {

        }
    }

}
