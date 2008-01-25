package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;
import java.awt.*;


/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 4:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusChecker extends TimerTask {

    private List<BambooStatusListenerImpl> listenerList = new ArrayList<BambooStatusListenerImpl>();


    public synchronized void registerListener(BambooStatusListenerImpl listener) {
        listenerList.add(listener);
    }

    public synchronized void unregisterListener(BambooStatusListenerImpl listener) {
        listenerList.remove(listener);
    }

    public synchronized void run() {
        // for each server
        Collection<BambooBuild> newStatus = null;
        for (int maxTries = 1; maxTries > 0; maxTries--) {
            try {
                newStatus = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults();
            } catch (ServerPasswordNotProvidedException exception) {
//                PasswordDialog dialog = new PasswordDialog(ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer());
//                dialog.pack();
//                JPanel panel = dialog.getPasswordPanel();
//
//                int answer = JOptionPane.showConfirmDialog(
//                        null, panel, PluginInfo.NAME , OK_CANCEL_OPTION,
//                        PLAIN_MESSAGE
//                );
//                String password = "";
//                Boolean shouldPasswordBeStored = false;
//                if (answer == JOptionPane.OK_OPTION) {
//                    password = dialog.getPasswordString();
//                    shouldPasswordBeStored = dialog.getShouldPasswordBeStored();
//                } else {
//
//                    JOptionPane.showMessageDialog(null, "You can always change password by changing plugin settings (Preferences | IDE Settings | "+ PluginInfo.NAME +")");
//                }
//                // so or so we assume that user provided password
//                ((ServerBean) (ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer()))
//                        .setPasswordString(password, shouldPasswordBeStored);
//                ((ServerBean) (ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer()))
//                        .setIsConfigInitialized(true);

				throw new IllegalArgumentException(exception);
			}
        }
        // end for
        for (BambooStatusListenerImpl listener : listenerList) {
            //listener.updateBuildStatuses(newStatus);

			listener.setBuilds(newStatus);
			EventQueue.invokeLater(listener);
		}

    }

}
