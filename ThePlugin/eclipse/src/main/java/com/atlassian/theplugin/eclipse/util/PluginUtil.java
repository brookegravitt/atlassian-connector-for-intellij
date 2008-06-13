/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	
	public static final String ICON_BAMBOO_RUN = "Run_bamboo_build";
	public static final String ICON_BAMBOO_LABEL = "Label_bamboo_build";
	public static final String ICON_BAMBOO_COMMENT = "Comment_bamboo_build";
	public static final String ICON_BAMBOO_REFRESH = "Refresh_bamboo_builds";
	
	public static final String ICON_BAMBOO_UNKNOWN = BuildStatus.UNKNOWN.toString();
	public static final String ICON_BAMBOO_FAILED = BuildStatus.BUILD_FAILED.toString();
	public static final String ICON_BAMBOO_SUCCEEDED = BuildStatus.BUILD_SUCCEED.toString();
	
	private static ImageRegistry imageRegistry;
	
	private static Logger logger = LoggerImpl.getInstance();	// default logger

	private PluginUtil() {
	}
	
	public static String getPluginName() {
		return PLUGIN_NAME;
	}
	
	public static Logger getLogger() {
		return PluginUtil.logger;
	}
	
	public static void setLogger(Logger logger) {
		PluginUtil.logger = logger;
	}
	
	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			
			imageRegistry.put(ICON_BAMBOO_SUCCEEDED, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_plan_passed.gif"));
			imageRegistry.put(ICON_BAMBOO_FAILED, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_plan_failed.gif"));
			imageRegistry.put(ICON_BAMBOO_UNKNOWN, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_plan_unknown.gif"));

			imageRegistry.put(ICON_BAMBOO_RUN, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/run.gif"));
			imageRegistry.put(ICON_BAMBOO_LABEL, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_label.gif"));
			imageRegistry.put(ICON_BAMBOO_COMMENT, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_comment.png"));
			imageRegistry.put(ICON_BAMBOO_REFRESH, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/refresh.gif"));
		}
		
		return imageRegistry;
	}
	
}
