package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedExeption;
import com.atlassian.theplugin.idea.PasswordDialog;

import javax.swing.*;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        // for each server
        Collection<BambooBuild> newStatus = null;
        for (int maxTries = 1; maxTries > 0; maxTries--) {
            try {
                newStatus = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults();
            } catch (ServerPasswordNotProvidedExeption exeption) {
                PasswordDialog dialog = new PasswordDialog(ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer());
                dialog.pack();
                JPanel panel = dialog.getPasswordPanel();
                int answer = JOptionPane.showConfirmDialog(
                        null, panel, "Provide password", OK_CANCEL_OPTION,
                        PLAIN_MESSAGE
                );
                if (answer == JOptionPane.YES_OPTION) {
                    ((ServerBean) (ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer())).setIsConfigInitialized(true);
                    ((ServerBean) (ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer())).setPasswordString(dialog.getPasswordString(), dialog.getShouldPasswordBeStored());
                }
            }
        }
        // end for
        for (BambooStatusListener listener : listenerList) {
            listener.updateBuildStatuses(newStatus);
        }

    }
}
