package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.bamboo.MissingPasswordHandler;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.crucible.CrucibleServerFactory;
import com.atlassian.theplugin.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.crucible.RemoteReview;
import com.atlassian.theplugin.crucible.api.CrucibleLoginException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;


/**
 * IDEA-specific class that uses {@link com.atlassian.theplugin.crucible.CrucibleServerFactory} to retrieve builds info and
 * passes raw data to configured {@link com.atlassian.theplugin.crucible.CrucibleStatusListener}s.<p>
 * <p/>
 * Intended to be triggered by a {@link java.util.Timer} through the {@link #newTimerTask()}.<p>
 * <p/>
 * Thread safe.
 */
public class CrucibleStatusChecker {

	private final List<CrucibleStatusListener> listenerList = new ArrayList<CrucibleStatusListener>();

	public void registerListener(CrucibleStatusListener listener) {
		synchronized (listenerList) {
			listenerList.add(listener);
		}
	}

	public void unregisterListener(CrucibleStatusListener listener) {
		synchronized (listenerList) {
			listenerList.remove(listener);
		}
	}

	/**
	 * DO NOT use that method in 'dispatching thread' of IDEA. It can block GUI for several seconds.
	 */
	private void doRun() {
		// collect build info from each server
		final Collection<RemoteReview> reviews = new ArrayList<RemoteReview>();
		for (Server server :
                ConfigurationFactory.getConfiguration().getProductServers(
                        ServerType.CRUCIBLE_SERVER).getEnabledServers()) {
                            try {
                                reviews.addAll(
                                        CrucibleServerFactory.getCrucibleServerFacade().getActiveReviewsForUser(server));
                            } catch (ServerPasswordNotProvidedException exception) {
                                ApplicationManager.getApplication().invokeLater(
                                        new MissingPasswordHandler(), ModalityState.defaultModalityState());
                            } catch (CrucibleLoginException e) {
                                // @todo
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }

		// dispatch to the listeners
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				synchronized (listenerList) {
					for (CrucibleStatusListener listener : listenerList) {
						listener.updateReviews(reviews);
					}
				}
			}
		});
	}

	/**
	 * Create a new instance of {@link java.util.TimerTask} for {@link java.util.Timer} re-scheduling purposes.
	 *
	 * @return new instance of TimerTask
	 */
	public TimerTask newTimerTask() {
		return new TimerTask() {
			public void run() {
				doRun();
			}
		};
	}


}