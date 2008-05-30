package com.atlassian.theplugin.eclipse.actions.bamboo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.util.PluginUtil;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooBuildAdapterEclipse;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooToolWindow;

public abstract class BambooAction extends Action {
	protected BambooServerFacade bambooFacade;
	protected BambooToolWindow bambooToolWindow;
	
	public BambooAction(BambooToolWindow bambooToolWindowTable) {
		
		this.bambooToolWindow = bambooToolWindowTable;
		
		bambooFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
		
		setEnabled(false);	// action is disabled by default
	}
	
	/**
	 * Sets message in UI status bar. Should be called from within non-UI thread.
	 * @param message text to show in status bar
	 */
	protected void setUIMessage(final String message) {
		EclipseActionScheduler.getInstance().invokeLater(new Runnable() {

			public void run() {
				bambooToolWindow.setStatusBarText(message);
			}
			
		});
	}

	/**
	 * 
	 * @return build selected in bamboo table associated with the current action
	 */
	protected BambooBuildAdapterEclipse getBuild() {
		IStructuredSelection selection = 
			(IStructuredSelection) bambooToolWindow.getBambooToolWindowContent().getTableViewer().getSelection();
		final BambooBuildAdapterEclipse build = (BambooBuildAdapterEclipse) selection.getFirstElement();
		return build;
	}
	
	public abstract String getToolTipText();
	
	public abstract ImageDescriptor getImageDescriptor();

}
