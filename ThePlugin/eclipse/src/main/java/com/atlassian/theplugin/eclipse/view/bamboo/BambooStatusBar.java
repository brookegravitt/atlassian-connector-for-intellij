package com.atlassian.theplugin.eclipse.view.bamboo;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

import com.atlassian.theplugin.commons.bamboo.BambooPopupInfo;
import com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginUtil;


public class BambooStatusBar extends StatusLineContributionItem implements BambooStatusDisplay {
	
	public BambooStatusBar() {
		super(Activator.PLUGIN_ID + ".statusline");

		setImage(PluginUtil.getImageRegistry().get(PluginUtil.ICON_BAMBOO_UNKNOWN));
		//setToolTipText("PAZU");
		
		setActionHandler(new Action() {
			public void run() {
				try {
					Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().
						showView(Activator.PLUGIN_ID + ".viewmain");
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
	}


	public void updateBambooStatus(BuildStatus generalBuildStatus,
			BambooPopupInfo info) {
		switch(generalBuildStatus) {
		case BUILD_FAILED:
			setImage(PluginUtil.getImageRegistry().get(PluginUtil.ICON_BAMBOO_FAILED));
			setToolTipText("Some builds failed. Click to see details.");
			break;
		case BUILD_SUCCEED:
			setImage(PluginUtil.getImageRegistry().get(PluginUtil.ICON_BAMBOO_SUCCEEDED));
			setToolTipText("All builds currently passing.");
			break;
		case UNKNOWN:
		default:
			setImage(PluginUtil.getImageRegistry().get(PluginUtil.ICON_BAMBOO_UNKNOWN));
			setToolTipText("");
			break;
		}
		
		
	}
}
