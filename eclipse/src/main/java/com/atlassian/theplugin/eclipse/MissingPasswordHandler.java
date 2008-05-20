/**
 * 
 */
package com.atlassian.theplugin.eclipse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * @author Jacek
 *
 */
public class MissingPasswordHandler implements Runnable {

	/** 
	 * That method is designed to be run in UI thread.
	 */
	public void run() {
		MessageBox missingPassword = new MessageBox(Activator.getDefault().getShell(), 
				SWT.ICON_WARNING | SWT.OK);
		
		missingPassword.setText("Warning");
		missingPassword.setMessage("Missing password");
	}

}
