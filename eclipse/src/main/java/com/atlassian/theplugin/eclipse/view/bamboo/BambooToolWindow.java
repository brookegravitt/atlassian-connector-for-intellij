/**
 * 
 */
package com.atlassian.theplugin.eclipse.view.bamboo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.atlassian.theplugin.commons.bamboo.BambooStatusListener;
import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListener;
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
		labelBuildAction.setEnabled(true);
		commentBuildAction.setEnabled(true);
		getViewSite().getActionBars().getToolBarManager().update(true);
	}
	
	public void disableBambooBuildActions() {
		runBuildAction.setEnabled(false);
		labelBuildAction.setEnabled(false);
		commentBuildAction.setEnabled(false);
		
		getViewSite().getActionBars().getToolBarManager().update(true);
	}


}

