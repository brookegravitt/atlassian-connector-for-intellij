package com.atlassian.theplugin.eclipse.view.bamboo;

import java.util.Collection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooStatusListener;
import com.atlassian.theplugin.eclipse.view.popup.NotificationPopup;

public class BambooStatusPopupListener implements BambooStatusListener {
	
	NotificationPopup popup;

	public void updateBuildStatuses(Collection<BambooBuild> arg0) {
		
		if (popup != null) {
			popup.close();
		}

		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		
		popup = new NotificationPopup(shell);
		popup.open();
	}

	public void resetState() {
		if (popup != null) {
			popup.close();
		}
	}
}
