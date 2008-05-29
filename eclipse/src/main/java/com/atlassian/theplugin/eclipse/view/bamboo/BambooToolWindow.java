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
		
		// create bamboo status listeners which is also a tab component
		BambooStatusListener bambooListener = new BambooToolWindowContent(parent, this);

		// register listener
		Activator.getDefault().getBambooChecker().registerListener(bambooListener);

		
		//getViewSite().registerContextMenu(menuManager, selectionProvider)
		
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		this.runBuildAction = new RunBuildAction(); 
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
	
	public void setHeader(String text) {
		setContentDescription(text);
	}

	public IAction getRunBuildAction() {
		return runBuildAction;
	}

	public IAction getLabelBuildAction() {
		return labelBuildAction;
	}
	public IAction getCommentBuildAction() {
		return commentBuildAction;
	}

	public void enableBambooBuildActions() {
		runBuildAction.setEnabled(true);
		labelBuildAction.setEnabled(true);
		commentBuildAction.setEnabled(true);
		
		getViewSite().getActionBars().getToolBarManager().update(true);
		getViewSite().getActionBars().getToolBarManager().update(true);
		getViewSite().getActionBars().getToolBarManager().markDirty();
		getViewSite().getActionBars().getToolBarManager().markDirty();
		getViewSite().getActionBars().getToolBarManager().update(false);
		getViewSite().getActionBars().updateActionBars();
		getViewSite().getActionBars().updateActionBars();
		
	}

}

