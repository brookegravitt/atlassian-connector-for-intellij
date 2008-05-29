package com.atlassian.theplugin.eclipse.actions.bamboo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.atlassian.theplugin.eclipse.util.PluginUtil;

public class RefreshBuildsListAction extends Action {
	
	private static final String REFRESH_BUILD_LIST = "Refresh";

	@Override
	public void run() {
		super.run();
		
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO use eclipse resource handling
		return ImageDescriptor.createFromImage(PluginUtil.getImageRegistry().get(PluginUtil.REFRESH));
	}

	@Override
	public String getText() {
		return REFRESH_BUILD_LIST;
	}

	@Override
	public String getToolTipText() {
		return REFRESH_BUILD_LIST;
	}
	
	
	

}
