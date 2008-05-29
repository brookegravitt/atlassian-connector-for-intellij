/**
 * 
 */
package com.atlassian.theplugin.eclipse.view.bamboo;

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
		
		toolBarManager.add(new RunBuildAction());
		toolBarManager.add(new LabelBuildAction());
		toolBarManager.add(new CommentBuildAction());
		toolBarManager.add(new Separator());
		toolBarManager.add(new RefreshBuildsListAction());
		
		
		getViewSite().getActionBars().updateActionBars();
		
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

}
