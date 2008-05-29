package com.atlassian.theplugin.eclipse.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.ImageLoader;

import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.commons.util.LoggerImpl;

public final class PluginUtil {
	private static final String PLUGIN_NAME = "Attlasian IDE Eclipse plug-in";
	public static final String BAMBOO_RUN = "Run_bamboo_build";
	public static final String BAMBOO_LABEL = "Label_bamboo_build";
	public static final String BAMBOO_COMMENT = "Comment_bamboo_build";
	public static final String REFRESH = "Refresh_bamboo_builds";
	private static ImageRegistry imageRegistry;

	private PluginUtil() {
	}
	
	public static String getPluginName() {
		return PLUGIN_NAME;
	}
	
	public static Logger getLogger() {
		return LoggerImpl.getInstance();
	}
	
	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			
			imageRegistry.put(BuildStatus.BUILD_SUCCEED.toString(), ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_plan_passed.gif"));
			imageRegistry.put(BuildStatus.BUILD_FAILED.toString(), ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_plan_failed.gif"));
			imageRegistry.put(BuildStatus.UNKNOWN.toString(), ImageDescriptor.createFromFile(PluginUtil.class, "icons/bamboo_plan_unknown.gif"));

			imageRegistry.put(BAMBOO_RUN, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/run.gif"));
			imageRegistry.put(BAMBOO_LABEL, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_label.gif"));
			imageRegistry.put(BAMBOO_COMMENT, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_comment.png"));
			imageRegistry.put(REFRESH, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/refresh.gif"));
			
			
			
		}
		
		return imageRegistry;
	}
	
}
