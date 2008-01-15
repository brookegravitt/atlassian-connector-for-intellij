package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.api.bamboo.RecentBuildItem;

import javax.swing.event.EventListenerList;
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
        Collection<BambooBuildInfo> newStatus = new ArrayList<BambooBuildInfo>();
        newStatus.add(new RecentBuildItem("The Plugin", "Build 1", "TP_DEFAULT", "Successful", "123", "Bo tak", "dawno", "dlugo", "fajnie"));
        newStatus.add(new RecentBuildItem("The Plugin", "Build 2", "TP_TEST", "Successful", "125", "Bo tak", "dawno", "dlugo", "fajnie"));
        newStatus.add(new RecentBuildItem("Nie wiem", "Build 3", "COSTAM", "FAILED", "124", "Bo tak", "dawno", "dlugo", "do dupy"));
                                

        for (BambooStatusListener listener : listenerList) {
            listener.statusUpdated(newStatus);
        }

    }
}
