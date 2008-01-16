package com.atlassian.theplugin.bamboo;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BambooServerFacade {
    Collection<BambooProject> getProjectList();

    Collection<BambooPlan> getPlanList();

    Collection<BambooBuild> getRecentBuildItems();

    BambooBuild getLatestBuildForPlan(String planName);
}
