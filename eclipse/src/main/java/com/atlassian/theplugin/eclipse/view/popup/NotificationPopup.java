/*
 * Created on 14.11.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package com.atlassian.theplugin.eclipse.view.popup;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooStatusListener;

/**
 * @author Benjamin Pasero
 * @author Mik Kersten
 */
public class NotificationPopup extends AbstractNotificationPopup {

	public NotificationPopup(Display display) {
		super(display);
	}
	
	public NotificationPopup(Shell shell) {
		super(shell.getDisplay());
	}

	protected void createTitleArea(Composite parent) {
		((GridData) parent.getLayoutData()).heightHint = 24;

		Label titleCircleLabel = new Label(parent, SWT.NONE);
		titleCircleLabel.setText("Bamboo notification");
		titleCircleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		//titleCircleLabel.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		Label closeButton = new Label(parent, SWT.NONE);
		closeButton.setText("Close");
		closeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		closeButton.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		closeButton.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				close();
			}
		});
	}

	protected void createContentArea(Composite parent) {
		for (int i = 0; i < 5; i++) {
			Label l = new Label(parent, SWT.None);
			l.setText("News: " + i);
			l.setBackground(parent.getBackground());
		}
	}

	@Override
	protected String getPopupShellTitle() {
		return "Sample Notification";
	}

	public void resetState() {
	}
}