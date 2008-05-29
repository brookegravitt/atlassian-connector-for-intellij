package com.atlassian.theplugin.eclipse.actions.bamboo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.atlassian.theplugin.eclipse.util.PluginUtil;

public class CommentBuildAction extends Action {
	
	private static final String COMMENT_BUILD = "Comment Build";

	public CommentBuildAction() {
		super();
		
		setEnabled(false); // action is disabled by default
	}
	
	
	@Override
	public void run() {
		super.run();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO use eclipse resource handling
		return ImageDescriptor.createFromImage(PluginUtil.getImageRegistry().get(PluginUtil.BAMBOO_COMMENT));
	}

	@Override
	public String getText() {
		return COMMENT_BUILD;
	}

	@Override
	public String getToolTipText() {
		return COMMENT_BUILD;
	}

}
