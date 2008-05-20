/**
 * 
 */
package com.atlassian.theplugin.eclipse.view;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.eclipse.bamboo.BambooTabDisplay;
import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * @author Jacek
 *
 */
public class ToolWindow extends ViewPart {

	/**
	 * 
	 */
	public ToolWindow() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		

		// add display component to the current window (view)
		BambooTabDisplay bambooTabDisplay = new BambooTabDisplay(parent);

		//BambooTabListener bambooTabListener = new BambooTabListener(bambooTabDisplay);

		// create bamboo status listeners
		HtmlBambooStatusListener bambooListener = new HtmlBambooStatusListener(bambooTabDisplay, 
				Activator.getDefault().getPluginConfiguration());
		
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
