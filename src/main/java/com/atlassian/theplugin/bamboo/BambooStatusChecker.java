package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.idea.PasswordDialog;
import com.atlassian.theplugin.idea.PluginInfo;

import javax.swing.*;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.apache.log4j.Category;


/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 4:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusChecker extends TimerTask {

	private final static Category logger = Logger.getInstance(BambooStatusChecker.class);

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
				showBlockingDialog();
			}
        }
		
		for (BambooStatusListenerImpl listener : listenerList) {
            //listener.updateBuildStatuses(newStatus);

			listener.setBuilds(newStatus);
			EventQueue.invokeLater(listener);
		}

    }

	private void showBlockingDialog() {

		MissingPasswordHandler handler = new MissingPasswordHandler();

		EventQueue.invokeLater(handler);

//		try {
//			EventQueue.invokeAndWait(handler);
//		} catch (InterruptedException e) {
//			logger.warn("Missing password dialog problem", e);
//		} catch (InvocationTargetException e) {
//			logger.warn("Missing password dialog problem", e);
//		}
	}

}
