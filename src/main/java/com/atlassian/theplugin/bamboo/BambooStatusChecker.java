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
public class BambooStatusChecker implements Runnable {

    private List<BambooStatusListener> listenerList = new ArrayList<BambooStatusListener>();


    public synchronized void registerListener(BambooStatusListener listener) {
        listenerList.add(listener);
    }

    public synchronized void unregisterListener(BambooStatusListener listener) {
        listenerList.remove(listener);
    }

    public synchronized void run() {
        Collection<BambooBuild> newStatus = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults();
        for (BambooStatusListener listener : listenerList) {
            listener.updateBuildStatuses(newStatus);
        }

    }
}
