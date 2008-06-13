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
		return ImageDescriptor.createFromImage(PluginUtil.getImageRegistry().get(PluginUtil.ICON_BAMBOO_COMMENT));
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
