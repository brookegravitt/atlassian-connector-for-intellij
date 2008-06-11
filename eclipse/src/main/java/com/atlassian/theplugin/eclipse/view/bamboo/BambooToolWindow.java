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

/**
 * 
 */
package com.atlassian.theplugin.eclipse.view.bamboo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.atlassian.theplugin.commons.bamboo.BambooStatusTooltipListener;
import com.atlassian.theplugin.eclipse.actions.bamboo.CommentBuildAction;
import com.atlassian.theplugin.eclipse.actions.bamboo.LabelBuildAction;
import com.atlassian.theplugin.eclipse.actions.bamboo.RefreshBuildsListAction;
import com.atlassian.theplugin.eclipse.actions.bamboo.RunBuildAction;
import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * @author Jacek
 *
 */
public class BambooToolWindow extends ViewPart {

	private IAction runBuildAction;
	private IAction labelBuildAction;
	private IAction commentBuildAction;
	private BambooToolWindowContent bambooToolWindowContent;
	
	private BambooStatusTooltipListener popupListener;

	/**
	 * 
	 */
	public BambooToolWindow() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		bambooToolWindowContent = new BambooToolWindowContent(parent, this);
		
		// register listener
		Activator.getDefault().getBambooChecker().registerListener(bambooToolWindowContent);
		
		// create and register popup listener
		BambooStatusTooltip popup = new BambooStatusTooltip();
		popupListener = new BambooStatusTooltipListener(popup, Activator.getDefault().getPluginConfiguration());
		Activator.getDefault().getBambooChecker().registerListener(popupListener);
		
		//getViewSite().registerContextMenu(menuManager, selectionProvider)
		
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		this.runBuildAction = new RunBuildAction(this); 
		this.labelBuildAction = new LabelBuildAction();
		this.commentBuildAction = new CommentBuildAction();
		
		toolBarManager.add(runBuildAction);
		toolBarManager.add(labelBuildAction);
		toolBarManager.add(commentBuildAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(new RefreshBuildsListAction());
		
		//getViewSite().getActionBars().updateActionBars();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}
	
	public void setHeaderText(String text) {
		setContentDescription(text);
	}
	
	public void setStatusBarText(String text) {
		getViewSite().getActionBars().getStatusLineManager().setMessage(text);
	}

	public BambooToolWindowContent getBambooToolWindowContent() {
		return bambooToolWindowContent;
	}

	public void enableBambooBuildActions() {
		runBuildAction.setEnabled(true);
		getViewSite().getActionBars().getToolBarManager().update(true);
	}
	
	public void enableBamboo2BuildActions() {
		//labelBuildAction.setEnabled(true);
		//commentBuildAction.setEnabled(true);
		getViewSite().getActionBars().getToolBarManager().update(true);
	}
	
	public void disableBambooBuildActions() {
		runBuildAction.setEnabled(false);
		labelBuildAction.setEnabled(false);
		commentBuildAction.setEnabled(false);
		
		getViewSite().getActionBars().getToolBarManager().update(true);
	}

	@Override
	public void dispose() {
		super.dispose();
		
		if (bambooToolWindowContent != null) {
			Activator.getDefault().getBambooChecker().unregisterListener(bambooToolWindowContent);
		}
	}

	
	
}

