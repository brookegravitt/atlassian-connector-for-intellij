package com.atlassian.theplugin.bamboo;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 4:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class Organik implements Runnable {

    private List<BambooStatusListener> listenerList = new ArrayList<BambooStatusListener>();


    public void registerListener(BambooStatusListener listener) {
        listenerList.add(listener);
    }

    public void unregisterListener(BambooStatusListener listener) {
        listenerList.remove(listener);
    }

    public void run() {
        BambooServerFacade bamboo = new BambooServerFactory().getBambooServerFacade();
        Collection<BambooBuild> newStatus = bamboo.getRecentBuildItems();

        for (BambooStatusListener listener : listenerList) {
            listener.statusUpdated(newStatus);
        }

    }
}
