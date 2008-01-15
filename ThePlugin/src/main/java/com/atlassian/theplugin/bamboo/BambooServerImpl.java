package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.api.bamboo.RecentBuildItem;

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

    public Collection<BambooBuildInfo> getRecentBuildItems() {
        Collection<BambooBuildInfo> newStatus = new ArrayList<BambooBuildInfo>();
        newStatus.add(new RecentBuildItem("The Plugin", "Build 1", "TP_DEFAULT", "Successful", "123", "Bo tak", "dawno", "dlugo", "fajnie"));
        newStatus.add(new RecentBuildItem("The Plugin", "Build 2", "TP_TEST", "Successful", "125", "Bo tak", "dawno", "dlugo", "fajnie"));
        newStatus.add(new RecentBuildItem("Nie wiem", "Build 3", "COSTAM", "Failed", "124", "Bo tak", "dawno", "dlugo", "do dupy"));

        return newStatus;
    }
}
