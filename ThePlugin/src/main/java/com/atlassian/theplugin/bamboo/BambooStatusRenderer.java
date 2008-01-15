package com.atlassian.theplugin.bamboo;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:49:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusRenderer implements BambooStatusListener {
    private int counter = 0;

    BambooDisplayComponent displayComponent;
    public BambooStatusRenderer(BambooDisplayComponent display) {
        displayComponent = display;

    }

    public void statusUpdated(Collection<BambooBuildInfo> stats) {
        String status = "newStatus " + ++counter;
        String statusExt = "Nowy dlugi status";

        displayComponent.updateBambooStatus(status, statusExt);
    }
}
