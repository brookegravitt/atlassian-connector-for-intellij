/**
 * 
 */
package com.atlassian.theplugin.eclipse;

import org.eclipse.swt.SWTException;

import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

/**
 * @author Jacek
 *
 */
public final class EclipseActionScheduler implements UIActionScheduler {

	private static UIActionScheduler instance = new EclipseActionScheduler();

	private EclipseActionScheduler() {
	}

	public static UIActionScheduler getInstance() {
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.commons.UIActionScheduler#invokeLater(java.lang.Runnable)
	 */
	public void invokeLater(Runnable runnable) {
		try {
			Activator.getDefault().getWorkbench().getDisplay().asyncExec(runnable);
		} catch (SWTException ex) {
			PluginUtil.getLogger().warn(ex);
		} catch (NullPointerException ex) {
			PluginUtil.getLogger().warn(ex);
		}
	}

}
