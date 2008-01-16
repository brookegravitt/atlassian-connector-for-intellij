package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.bamboo.BambooProjectInfo;
import com.atlassian.theplugin.bamboo.BambooPlanInfo;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:12:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooServerImpl implements BambooServerFacade {

    public Collection<BambooProject> getProjectList() {
        Collection<BambooProject> newProject = new ArrayList<BambooProject>();
        newProject.add(new BambooProjectInfo("The Plugin", "TP"));
        newProject.add(new BambooProjectInfo("API Test Project", "APITEST"));
        return newProject;
    }

    public Collection<BambooPlan> getPlanList() {
        Collection<BambooPlan> newProject = new ArrayList<BambooPlan>();
        newProject.add(new BambooPlanInfo("Default", "TP-DEF"));
        newProject.add(new BambooPlanInfo("TestPlan", "TP-TEST"));
        newProject.add(new BambooPlanInfo("Default", "APITEST-DEF"));
        return newProject;
    }

    public Collection<BambooBuild> getRecentBuildItems() {
        Collection<BambooBuild> newStatus = new ArrayList<BambooBuild>();
        newStatus.add(new BambooBuildInfo("The Plugin", "Build 1", "TP_DEFAULT", "Successful", "123", "Bo tak", "dawno", "dlugo", "fajnie"));
        newStatus.add(new BambooBuildInfo("The Plugin", "Build 2", "TP_TEST", "Successful", "125", "Bo tak", "dawno", "dlugo", "fajnie"));
        newStatus.add(new BambooBuildInfo("Nie wiem", "Build 3", "COSTAM", "Failed", "124", "Bo tak", "dawno", "dlugo", "do dupy"));

        return newStatus;
    }

    public BambooBuild getLatestBuildForPlan(String planName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }    
}
