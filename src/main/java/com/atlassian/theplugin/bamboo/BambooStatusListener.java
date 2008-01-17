package com.atlassian.theplugin.bamboo;

import java.util.EventListener;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:31:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BambooStatusListener extends EventListener {
    void updateBuildStatuses(Collection<BambooBuild> stats);
}
