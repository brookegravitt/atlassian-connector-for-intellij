package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.bamboo.BuildStatus;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-12
 * Time: 17:16:04
 * To change this template use File | Settings | File Templates.
 */
public interface CrucibleStatusDisplay {
    void updateCrucibleStatus(String htmlPage);
}
