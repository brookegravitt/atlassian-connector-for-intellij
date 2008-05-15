package com.atlassian.theplugin.eclipse.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.commons.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.configuration.SubscribedPlanBean;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.EclipseLogger;
import com.atlassian.theplugin.eclipse.MissingPasswordHandler;
import com.atlassian.theplugin.eclipse.util.PluginUtil;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.theplugin.eclipse";

	// The shared instance
	private static Activator plugin;
	
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
//		IWorkbenchPartSite site = this.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite();
//		IViewSite viewSite = (IViewSite) site;
//		viewSite.getActionBars().getStatusLineManager().setMessage("dupa maryni");
		
		// create configuration
		ProjectConfigurationWrapper configurationWrapper = new ProjectConfigurationWrapper(getPluginPreferences());
		PluginConfigurationBean pluginConfiguration = configurationWrapper.getPluginConfiguration();
		
		// create logger
		new EclipseLogger(getLog());	// now you can use PluginUtil.getLogger

		// create checker
		MissingPasswordHandler missingPasswordHandler = new MissingPasswordHandler();
		BambooStatusChecker bambooChecker = BambooStatusChecker.getInstance(
				EclipseActionScheduler.getInstance(), pluginConfiguration, missingPasswordHandler, PluginUtil.getLogger());
		
		Timer timer = new Timer("Atlassian Eclipse Plugin checkers");
		
		timer.schedule(bambooChecker.newTimerTask(), 0);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
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
}
