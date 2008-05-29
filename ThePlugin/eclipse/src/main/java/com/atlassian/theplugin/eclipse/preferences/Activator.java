package com.atlassian.theplugin.eclipse.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.atlassian.theplugin.commons.SchedulableChecker;
import com.atlassian.theplugin.commons.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.EclipseLogger;
import com.atlassian.theplugin.eclipse.MissingPasswordHandler;
import com.atlassian.theplugin.eclipse.util.PluginUtil;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static final int BAMBOO_CHECKER_POLLING = 60000;

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.theplugin.eclipse";

	// The shared instance
	private static Activator plugin;

	private Timer timer;

	private PluginConfigurationBean pluginConfiguration;

	private BambooStatusChecker bambooChecker;

	private Collection<TimerTask> scheduledComponents = new ArrayList<TimerTask>();

	private Collection<SchedulableChecker> schedulableCheckers = new ArrayList<SchedulableChecker>();
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		// create configuration
		ProjectConfigurationWrapper configurationWrapper = new ProjectConfigurationWrapper(getPluginPreferences());
		pluginConfiguration = configurationWrapper.getPluginConfiguration();
		ConfigurationFactory.setConfiguration(pluginConfiguration);
		
		// create logger
		new EclipseLogger(getLog());	// now you can use PluginUtil.getLogger

		// create bamboo checker
		MissingPasswordHandler missingPasswordHandler = new MissingPasswordHandler();
		bambooChecker = BambooStatusChecker.getInstance(
				EclipseActionScheduler.getInstance(), pluginConfiguration, missingPasswordHandler, PluginUtil.getLogger());
		schedulableCheckers.add(bambooChecker);
		
		// create timer
		timer = new Timer("Atlassian Eclipse Plugin checkers");
		
		// start timer/checkers
		startTimer();
		//timer.schedule(bambooChecker.newTimerTask(), 0, BAMBOO_CHECKER_POLLING);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
		timer.cancel();
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public Timer getTimer() {
		return timer;
	}

	public PluginConfigurationBean getPluginConfiguration() {
		return pluginConfiguration;
	}

	public BambooStatusChecker getBambooChecker() {
		return bambooChecker;
	}
	
	public Shell getShell() {
		return this.getWorkbench().getDisplay().getActiveShell();
	}
	
	private void disableTimer() {
		
		Iterator<TimerTask> i = scheduledComponents.iterator();
		while (i.hasNext()) {
			TimerTask timerTask = i.next();
			i.remove();
			timerTask.cancel();
		}

		timer.purge();
	}
	
	private void startTimer() {
		for (SchedulableChecker checker : schedulableCheckers) {
			if (checker.canSchedule()) {
				final TimerTask newTask = checker.newTimerTask();
				scheduledComponents.add(newTask);
				timer.schedule(newTask, 0, BAMBOO_CHECKER_POLLING);
				//timer.schedule(newTask, 0, checker.getInterval());
			} else {
				checker.resetListenersState();
			}
		}
	}
	
	public void rescheduleStatusCheckers() {

		disableTimer();
		
		startTimer();

	}


	
}
