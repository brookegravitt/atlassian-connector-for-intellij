package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.bamboo.MissingPasswordHandler;
import com.atlassian.theplugin.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.crucible.ReviewDataInfo;
import com.atlassian.theplugin.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.idea.SchedulableComponent;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.DateUtil;
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
public final class CrucibleStatusChecker implements SchedulableComponent {
	private final List<CrucibleStatusListener> listenerList = new ArrayList<CrucibleStatusListener>();
	private final PluginConfiguration pluginConfiguration;
	private final CrucibleServerFacade crucibleServerFacade;

	public CrucibleStatusChecker(PluginConfiguration pluginConfiguration, CrucibleServerFacade crucibleServerFacade) {
		this.pluginConfiguration = pluginConfiguration;
		this.crucibleServerFacade = crucibleServerFacade;
	}

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
        try {
			// collect build info from each server
            final Collection<ReviewDataInfo> reviews = new ArrayList<ReviewDataInfo>();
            for (Server server : retrieveEnabledCrucibleServers()) {
                                try {
									PluginUtil.getLogger().debug("Crucible: updating status for server: "
											+ server.getUrlString());
									reviews.addAll(
                                            crucibleServerFacade.getActiveReviewsForUser(server));
                                } catch (ServerPasswordNotProvidedException exception) {
                                    ApplicationManager.getApplication().invokeLater(
                                            new MissingPasswordHandler(), ModalityState.defaultModalityState());
                                } catch (CrucibleLoginException e) {
                                    PluginUtil.getLogger().warn("Error getting Crucible reviews for " + server.getName()
											+ " server", e);
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
	    } catch (Throwable t) {
			t.printStackTrace();
        }
    }

	private Collection<Server> retrieveEnabledCrucibleServers() {
		return pluginConfiguration.getProductServers(
                            ServerType.CRUCIBLE_SERVER).getEnabledServers();
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

	public boolean canSchedule() {
		return !retrieveEnabledCrucibleServers().isEmpty();
	}

	public long getInterval() {
		return (long) ((CrucibleConfigurationBean) pluginConfiguration
				.getProductServers(ServerType.CRUCIBLE_SERVER))
				.getPollTime() * DateUtil.SECONDS_IN_MINUTE * DateUtil.MILISECONDS_IN_SECOND;
	}
}