package com.atlassian.theplugin.eclipse.view.bamboo;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.atlassian.theplugin.commons.bamboo.BambooPopupInfo;
import com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.eclipse.view.popup.NotificationPopup;

public class BambooStatusTooltip implements BambooStatusDisplay {
	
	NotificationPopup popup;

	public void updateBambooStatus(BuildStatus status, BambooPopupInfo popupInfo) {
		
		if (popup != null) {
			popup.close();
		}

		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		
		popup = new NotificationPopup(shell);
		popup.setContent(status, popupInfo);
		popup.open();
	}
}
