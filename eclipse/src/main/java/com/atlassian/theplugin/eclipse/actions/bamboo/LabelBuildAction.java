package com.atlassian.theplugin.eclipse.actions.bamboo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.atlassian.theplugin.eclipse.util.PluginUtil;

public class LabelBuildAction extends Action {
	
	private static final String LABEL_BUILD = "Label Build";

	@Override
	public void run() {
		super.run();
		
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO use eclipse resource handling
		return ImageDescriptor.createFromImage(PluginUtil.getImageRegistry().get(PluginUtil.BAMBOO_LABEL));
	}

	@Override
	public String getText() {
		return LABEL_BUILD;
	}

	@Override
	public String getToolTipText() {
		return LABEL_BUILD;
	}
	
	
	

}
