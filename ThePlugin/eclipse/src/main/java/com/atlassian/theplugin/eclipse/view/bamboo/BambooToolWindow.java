/**
 * 
 */
package com.atlassian.theplugin.eclipse.view.bamboo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.atlassian.theplugin.commons.bamboo.BambooStatusListener;
import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListener;
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
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		// create bamboo status listeners
		BambooStatusListener bambooListener = new BambooToolWindowContent(parent);
		//(bambooTabContent, Activator.getDefault().getPluginConfiguration());
		
		Activator.getDefault().getBambooChecker().registerListener(bambooListener);
		

		
		//this.getSite().getWorkbenchWindow()

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
