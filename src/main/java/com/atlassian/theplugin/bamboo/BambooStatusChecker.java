package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.PasswordDialog;
import com.atlassian.theplugin.idea.PluginInfo;

import javax.swing.*;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;


/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 4:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusChecker extends TimerTask {

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
            } catch (ServerPasswordNotProvidedException exception) {
                PasswordDialog dialog = new PasswordDialog(ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer());
                dialog.pack();
                JPanel panel = dialog.getPasswordPanel();

                int answer = JOptionPane.showConfirmDialog(
                        null, panel, PluginInfo.NAME , OK_CANCEL_OPTION,
                        PLAIN_MESSAGE
                );
                String password = "";
                Boolean shouldPasswordBeStored = false;
                if (answer == JOptionPane.OK_OPTION) {
                    password = dialog.getPasswordString();
                    shouldPasswordBeStored = dialog.getShouldPasswordBeStored();
                } else {

                    JOptionPane.showMessageDialog(null, "You can always change password by changing plugin settings (Preferences | IDE Settings | "+ PluginInfo.NAME +")");
                }
                // so or so we assume that user provided password
                ((ServerBean) (ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer()))
                        .setPasswordString(password, shouldPasswordBeStored);
                ((ServerBean) (ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer()))
                        .setIsConfigInitialized(true);
            }
        }
        // end for
        for (BambooStatusListener listener : listenerList) {
            listener.updateBuildStatuses(newStatus);
        }

    }

}
