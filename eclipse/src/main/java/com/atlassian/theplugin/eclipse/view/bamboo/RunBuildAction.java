package com.atlassian.theplugin.eclipse.view.bamboo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.atlassian.theplugin.eclipse.util.PluginUtil;

public class RunBuildAction extends Action {
	
	public void RunBuildAction() {
		setText("Run Build");
		setToolTipText("Run Build");
		// TODO use eclipse resource handling
		setImageDescriptor(ImageDescriptor.createFromImage(PluginUtil.getImageRegistry().get(PluginUtil.BAMBOO_RUN)));
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_plan_passed.gif"); 
		//ImageDescriptor.createFromImage(PluginUtil.getImageRegistry().get(PluginUtil.BAMBOO_RUN));
	}

	@Override
	public String getText() {
		return "Run Build";
	}

	@Override
	public String getToolTipText() {
		return "Run Build";
	}
	
	

}
