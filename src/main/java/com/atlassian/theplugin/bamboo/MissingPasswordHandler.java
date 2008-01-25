package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.idea.PasswordDialog;
import com.atlassian.theplugin.idea.PluginInfo;

import javax.swing.*;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-25
 * Time: 11:20:12
 * To change this template use File | Settings | File Templates.
 */
public class MissingPasswordHandler implements Runnable {

    private static boolean isDialogShown = false;

    public void run() {

        ServerBean conf = (ServerBean) (ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer());

        if (!isDialogShown) {

            isDialogShown = true;

            PasswordDialog dialog = new PasswordDialog(
                    ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer());
            dialog.pack();
            JPanel panel = dialog.getPasswordPanel();

            //WindowManager.getInstance().getAllFrames();

            int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), panel,
                    PluginInfo.NAME, OK_CANCEL_OPTION, PLAIN_MESSAGE);

            String password = "";
            Boolean shouldPasswordBeStored = false;
            if (answer == JOptionPane.OK_OPTION) {
                password = dialog.getPasswordString();
                shouldPasswordBeStored = dialog.getShouldPasswordBeStored();
            } else {

                JOptionPane.showMessageDialog(null,
                        "You can always change password by changing plugin settings (Preferences | IDE Settings | "
                                + PluginInfo.NAME + ")");
            }
            // so or so we assume that user provided password
            conf.setPasswordString(password, shouldPasswordBeStored);
            conf.setIsConfigInitialized(true);
        }

    }
}
