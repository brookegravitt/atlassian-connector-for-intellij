/**
 * 
 */
package com.atlassian.theplugin.eclipse;

import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * @author Jacek
 *
 */
public class EclipseActionScheduler implements UIActionScheduler {

	private static UIActionScheduler instance = new EclipseActionScheduler();

	private EclipseActionScheduler(){}

	public static UIActionScheduler getInstance() {
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.commons.UIActionScheduler#invokeLater(java.lang.Runnable)
	 */
	public void invokeLater(Runnable runnable) {
		Activator.getDefault().getWorkbench().getDisplay().asyncExec(runnable);
	}

}
