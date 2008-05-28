package com.atlassian.theplugin.eclipse.view.bamboo;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.eclipse.preferences.Activator;

public class BambooToolWindowHtmlContent implements BambooStatusDisplay {

	private Composite parent;

	private Browser htmlBrowser;

	private static boolean linkClicked = false;

	private static String html;

	public BambooToolWindowHtmlContent(Composite parent) {
		this.parent = parent;
		htmlBrowser = new Browser(parent, SWT.NONE);

		htmlBrowser.addMouseListener(new MouseClickListener());
		htmlBrowser
				.addLocationListener(new BrowserLocationListener(htmlBrowser));
	}

	public void updateBambooStatus(BuildStatus arg0, String aHtml) {
		// TODO Auto-generated method stub
		this.html = aHtml;
		linkClicked = false;
		htmlBrowser.setText(html);
	}

	private class MouseClickListener extends MouseAdapter {

		public MouseClickListener() {
			super();
			System.out.print("");
		}

		@Override
		public void mouseUp(MouseEvent e) {
			super.mouseUp(e);
			// mouse clicked
			BambooToolWindowHtmlContent.linkClicked = true;
			System.out.println("click");
		}
	}

	private class BrowserLocationListener extends LocationAdapter {

		private Browser browser;

		public BrowserLocationListener(Browser htmlBrowser) {
			super();
			this.browser = htmlBrowser;
		}

		@Override
		public void changing(LocationEvent event) {
			super.changing(event);
			if (BambooToolWindowHtmlContent.linkClicked) {
				try {
					Activator.getDefault().getWorkbench().getBrowserSupport()
							.createBrowser(
									IWorkbenchBrowserSupport.AS_EXTERNAL,
									"aCustomId", "url", "url").openURL(
									new URL(event.location));
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				browser.stop();
				browser.setText(html);
				BambooToolWindowHtmlContent.linkClicked = false;
				System.out.println("changing");
			}
		}

	}

}
