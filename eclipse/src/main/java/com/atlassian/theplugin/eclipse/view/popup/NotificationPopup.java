/*
 * Created on 14.11.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package com.atlassian.theplugin.eclipse.view.popup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooPopupInfo;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

/**
 * @author Benjamin Pasero
 * @author Mik Kersten
 */
public class NotificationPopup extends AbstractNotificationPopup {

	private BambooPopupInfo content = new BambooPopupInfo();

	public NotificationPopup(Display display) {
		super(display);
	}
	
	public NotificationPopup(Shell shell) {
		super(shell.getDisplay());
	}

	protected void createTitleArea(Composite parent) {
		
//		((GridData) parent.getLayoutData()).heightHint = TITLE_HEIGHT;
//
//		Label titleImageLabel = new Label(parent, SWT.NONE);
//		titleImageLabel.setImage(getPopupShellImage(TITLE_HEIGHT));
//
//		Label titleTextLabel = new Label(parent, SWT.NONE);
//		titleTextLabel.setText(getPopupShellTitle());
//		titleTextLabel.setFont(TaskListColorsAndFonts.BOLD);
//		titleTextLabel.setForeground(color.getTitleText());
//		titleTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
//		titleTextLabel.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
//
//		Label button = new Label(parent, SWT.NONE);
//		button.setImage(TasksUiImages.getImage(TasksUiImages.NOTIFICATION_CLOSE));
//
//		button.addMouseListener(new MouseListener() {
//
//			public void mouseDoubleClick(MouseEvent e) {
//				// ignore
//			}
//
//			public void mouseDown(MouseEvent e) {
//				// ignore
//			}
//
//			public void mouseUp(MouseEvent e) {
//				close();
//			}
//
//		});
		
		
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
		
		for (BambooBuild build : content.getBambooBuilds()) {
			
			Composite notificationComposite = new Composite(parent, SWT.NO_FOCUS);
			notificationComposite.setLayout(new GridLayout(2, false));
			notificationComposite.setBackground(parent.getBackground());

			Label image = new Label(notificationComposite, SWT.NO_FOCUS);
			image.setText("example build");
			
			String icon;
			String st;
		
			switch (build.getStatus()) {
				case BUILD_SUCCEED:
					icon = PluginUtil.ICON_BAMBOO_SUCCEEDED;
					st = "succeeded";
					break;
				case BUILD_FAILED:
					icon = PluginUtil.ICON_BAMBOO_FAILED;
					st = "failed";
					break;
				case BUILD_DISABLED:
				default:
					icon = PluginUtil.ICON_BAMBOO_UNKNOWN;
					st = "unknown";
					break;
			}
			
			image.setImage(PluginUtil.getImageRegistry().get(icon));
			image.setBackground(parent.getBackground());

			Label l2 = new Label(notificationComposite, SWT.NO_FOCUS);
			l2.setText(build.getBuildKey() + " " + build.getBuildNumber() + " " + st);
			l2.setBackground(parent.getBackground());

		}

		//parent.setLayout(new FillLayout());
		
		//Browser b = new Browser(parent, SWT.NONE);
		//b.setText(content);
		//b.setBackground(new Color(new Device(), SWT.COLOR_MAGENTA));
//			Label l = new Label(parent, SWT.None);
//			l.setText(contentHtml);
//			l.setBackground(parent.getBackground());
	}

	@Override
	protected String getPopupShellTitle() {
		return "Sample Notification";
	}

	public void resetState() {
	}
	
	public void setContent(BambooPopupInfo popupInfo) {
		this.content = popupInfo;
	}
}