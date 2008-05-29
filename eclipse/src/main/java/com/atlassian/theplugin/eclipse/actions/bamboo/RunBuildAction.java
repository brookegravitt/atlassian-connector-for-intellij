package com.atlassian.theplugin.eclipse.actions.bamboo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.atlassian.theplugin.eclipse.util.PluginUtil;

public class RunBuildAction extends Action {
	
	private static final String RUN_BUILD = "Run Build";

	public RunBuildAction() {
		super();
		setEnabled(false);	// action is disabled by default
	}
	
	
	@Override
	public void run() {
		super.run();

	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO use eclipse resource handling
		return ImageDescriptor.createFromImage(PluginUtil.getImageRegistry().get(PluginUtil.BAMBOO_RUN));
	}

	@Override
	public String getText() {
		return RUN_BUILD;
	}

	@Override
	public String getToolTipText() {
		return RUN_BUILD;
	}	

}
