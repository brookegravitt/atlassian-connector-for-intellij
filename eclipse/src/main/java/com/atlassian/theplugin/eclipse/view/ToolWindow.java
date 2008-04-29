/**
 * 
 */
package com.atlassian.theplugin.eclipse.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

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
		// TODO Auto-generated method stub
		Label label = new Label(parent, SWT.LEFT);
		label.setText("atlassian-eclipse-plugin test label");
		
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
