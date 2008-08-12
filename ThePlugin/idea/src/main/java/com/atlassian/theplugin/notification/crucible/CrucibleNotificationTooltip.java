/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.bamboo.StausIconBambooListener;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusIcon;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CrucibleNotificationTooltip implements CrucibleNotificationListener {
	private final CrucibleStatusIcon display;
	private static final Color BACKGROUND_COLOR = new Color(255, 255, 200);
	private Project project;
	private boolean exceptionRaised = false;

	public CrucibleNotificationTooltip(CrucibleStatusIcon display, Project project) {
		this.display = display;
		this.project = project;
	}


	//private List<CrucibleNotification>
	public void updateNotifications(java.util.List<CrucibleNotification> notifications) {
		exceptionRaised = false;
		if (!notifications.isEmpty()) {
			StringBuilder sb = new StringBuilder("<table width=\"100%\">");

			int newExceptionCount = 0;
			StringBuilder nsb = new StringBuilder();
			for (CrucibleNotification notification : notifications) {
				if (notification instanceof NewExceptionNotification) {
					newExceptionCount++;
					nsb.append("<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\"")
							.append(notification.getItemUrl()).append("\">")
							.append("").append("</a></td><td>").append(notification.getPresentationMessage())
							.append("</td></tr>");
					exceptionRaised = true;
				}
			}

			if (newExceptionCount > 0) {
				sb.append("<tr><td width=20><img src=\"/icons/crucible-blue-16.png\" height=16 width=16 border=0></td>")
						.append("<td colspan=2><b>")
						.append(newExceptionCount)
						.append(" New Crucible Exception")
						.append(newExceptionCount != 1 ? "s" : "").append("</b></td></tr>");
				sb.append(nsb.toString());
			}

			nsb = new StringBuilder();
			int newReviewCount = 0;
			for (CrucibleNotification notification : notifications) {
				if (notification instanceof NewReviewNotification) {
					newReviewCount++;
					String id = notification.getId().getId();
					nsb.append("<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\"")
							.append(notification.getItemUrl()).append("\">")
							.append(id).append("</a></td><td>").append(notification.getPresentationMessage())
							.append("</td></tr>");
				}
			}

			if (newReviewCount > 0) {
				sb.append("<tr><td width=20><img src=\"/icons/crucible-blue-16.png\" height=16 width=16 border=0></td>")
						.append("<td colspan=2><b>")
						.append(newReviewCount)
						.append(" New Crucible Review")
						.append(newReviewCount != 1 ? "s" : "").append("</b></td></tr>");
				sb.append(nsb.toString());
			}


			int changesCount = notifications.size() - newReviewCount;
			if (changesCount > 0) {
				sb.append("<tr><td width=20><img src=\"/icons/crucible-blue-16.png\" height=16 width=16 border=0></td>")
						.append("<td colspan=2><b>")
						.append(changesCount)
						.append(" change")
						.append(changesCount != 1 ? "s" : "")
						.append("</b></td></tr>");

				for (CrucibleNotification notification : notifications) {
					if (!(notification instanceof NewReviewNotification)
							&& !(notification instanceof NewExceptionNotification)) {
						String id = notification.getId().getId();
						sb.append("<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\"")
								.append(notification.getItemUrl()).append("\">")
								.append(id).append("</a></td><td>")
								.append(notification.getPresentationMessage()).append("</td></tr>");
					}
				}
			}

			sb.append("</table>");
			if (project != null) {
				if (notifications.size() > 0) {
					display.triggerNewReviewAction(notifications.size(), exceptionRaised);

					JEditorPane content = new JEditorPane();
					content.setEditable(false);
					content.setContentType("text/html");
					content.setEditorKit(new ClasspathHTMLEditorKit());
					content.setText("<html>" + StausIconBambooListener.BODY_WITH_STYLE + sb.toString() + "</body></html>");
					content.setBackground(BACKGROUND_COLOR);
					content.addHyperlinkListener(new GenericHyperlinkListener());

					content.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							display.resetIcon();
							PluginToolWindow.focusPanel(project, PluginToolWindow.ToolWindowPanels.CRUCIBLE);
						}
					});
					content.setCaretPosition(0); // do this to make sure scroll pane is always at the top / header
					final WindowManager windowManager = WindowManager.getInstance();
					if (windowManager != null) {
						final StatusBar statusBar = windowManager.getStatusBar(project);
						if (statusBar != null) {
							statusBar.fireNotificationPopup(new JScrollPane(content), BACKGROUND_COLOR);
						}
					}
				}
			}
		}
	}

	public void resetState() {
		display.resetIcon();
	}
}
