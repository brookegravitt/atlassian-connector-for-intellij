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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;

import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.eclipse.dialogs.bamboo.LabelBuildDialog;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginUtil;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooBuildAdapterEclipse;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooToolWindow;

public class LabelBuildAction extends BambooAction {
	
	private static final String LABEL_BUILD = "Label Build";

	public LabelBuildAction(BambooToolWindow bambooToolWindow) {
		super(bambooToolWindow);
		
		setEnabled(false);	// action is disabled by default
	}
	
	@Override
	public void run() {
		super.run();
		
		LabelBuildDialog dialog = new LabelBuildDialog(Activator.getDefault().getShell());
		dialog.open();

		if (dialog.getReturnCode() == SWT.OK) {

			final String label = dialog.getLabel();
			final BambooBuildAdapterEclipse build = getBuild();

			Thread labelBuild = new Thread(new Runnable() {

				public void run() {
					try {
						setUIMessage("Labeling build on plan " + build.getBuildKey());
						bambooFacade.addLabelToBuild(
								build.getServer(), build.getBuildKey(), build.getBuildNumber(), label);
						setUIMessage("Build labeled on plan " + build.getBuildKey());
					} catch (ServerPasswordNotProvidedException e) {
						setUIMessage("Build not labeled. Password not provided for server");
					} catch (RemoteApiException e) {
						setUIMessage("Build not labeled. " + e.getMessage());
					}
				}

			}, "atlassian-eclipse-plugin: Label Build Action thread");

			labelBuild.start();
		}
		
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO use eclipse resource handling
		return ImageDescriptor.createFromImage(PluginUtil.getImageRegistry().get(PluginUtil.ICON_BAMBOO_LABEL));
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
