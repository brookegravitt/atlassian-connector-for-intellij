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
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.crucible.api.model.notification.AbstractCommentNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.AbstractUpdatedCommentNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotificationType;
import com.atlassian.theplugin.commons.crucible.api.model.notification.NewExceptionNotification;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusIcon;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class CrucibleNotificationTooltip implements CrucibleNotificationListener {
	private static final Color BACKGROUND_COLOR = new Color(255, 255, 200);

	private final CrucibleStatusIcon display;
	private final Project project;
	private final PluginToolWindow pluginToolWindow;

	private final Map<Server, NewExceptionNotification> exceptions = new HashMap<Server, NewExceptionNotification>();

	public CrucibleNotificationTooltip(@NotNull final CrucibleStatusIcon display, @NotNull final Project project,
			@NotNull final PluginToolWindow pluginToolWindow) {
		this.display = display;
		this.project = project;
		this.pluginToolWindow = pluginToolWindow;
	}


	//private List<CrucibleNotification>
	public void updateNotifications(java.util.List<CrucibleNotification> notifications) {
		boolean exceptionRaised = false;
		if (!notifications.isEmpty()) {
			StringBuilder sb = new StringBuilder("<table width=\"100%\">");

			int notificationCount = 0;
			int newExceptionCount = 0;
			StringBuilder nsb = new StringBuilder();
			for (CrucibleNotification notification : notifications) {
				if (notification.getType() == CrucibleNotificationType.EXCEPTION_RAISED) {
					NewExceptionNotification newNotification = (NewExceptionNotification) notification;
					NewExceptionNotification oldNotification = exceptions.get(newNotification.getServer());
					if (oldNotification == null
							|| !newNotification.equals(oldNotification)) {
						newExceptionCount++;
						notificationCount++;
						nsb.append("<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\"")
								.append(notification.getItemUrl()).append("\">")
								.append("").append("</a></td><td>").append(notification.getPresentationMessage())
								.append("</td></tr>");
						exceptionRaised = true;
					}
				}
			}
			exceptions.clear();
			for (CrucibleNotification notification : notifications) {
				if (notification.getType() == CrucibleNotificationType.EXCEPTION_RAISED) {
					NewExceptionNotification newNotification = (NewExceptionNotification) notification;
					exceptions.put(newNotification.getServer(), newNotification);
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
				if (notification.getType() == CrucibleNotificationType.NEW_REVIEW) {
					newReviewCount++;
					notificationCount++;
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


			RegularNotificationsProcessor regularNotificationsProcessor = new RegularNotificationsProcessor(notifications,
					notificationCount).invoke();
			int changesCount = regularNotificationsProcessor.getChangesCount();
			notificationCount = regularNotificationsProcessor.getNotificationCount();
			if (changesCount > 0) {
				sb.append("<tr><td width=20><img src=\"/icons/crucible-blue-16.png\" height=16 width=16 border=0></td>")
						.append("<td colspan=2><b>")
						.append(changesCount)
						.append(" change")
						.append(changesCount != 1 ? "s" : "")
						.append("</b></td></tr>");
				sb.append(regularNotificationsProcessor.getChanges());
			}

			sb.append("</table>");
			if (project != null) {
				if (notificationCount > 0) {
					display.triggerNewReviewAction(notifications.size(), exceptionRaised);

					JEditorPane content = new JEditorPane();
					content.setEditable(false);
					content.setContentType("text/html");
					content.setEditorKit(new ClasspathHTMLEditorKit());
					content.setText("<html>" + StausIconBambooListener.BODY_WITH_STYLE + sb.toString() + "</body></html>");
					content.setBackground(BACKGROUND_COLOR);
					content.addHyperlinkListener(new GenericHyperlinkListener());

					content.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							display.resetIcon();
							pluginToolWindow.focusPanel(PluginToolWindow.ToolWindowPanels.CRUCIBLE);
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

	private class RegularNotificationsProcessor {
		private final java.util.List<CrucibleNotification> notifications;
		private int notificationCount;
		private int changesCount;
		private StringBuilder changes;

		public RegularNotificationsProcessor(final java.util.List<CrucibleNotification> notifications,
				final int notificationCount) {
			this.notifications = notifications;
			this.notificationCount = notificationCount;
		}

		public int getNotificationCount() {
			return notificationCount;
		}

		public int getChangesCount() {
			return changesCount;
		}

		public String getChanges() {
			return changes.toString();
		}

		public RegularNotificationsProcessor invoke() {
			changesCount = 0;
			changes = new StringBuilder();
			for (CrucibleNotification notification : notifications) {
				CrucibleNotificationType type = notification.getType();
				String id = "";
				if (notification.getId() != null) {
					id = notification.getId().getId();
				}

				switch (type) {
					case EXCEPTION_RAISED:
					case NEW_REVIEW:
					case NOT_VISIBLE_REVIEW:
					case REVIEW_DATA_CHANGED:
						break;
					case NEW_GENERAL_COMMENT:
					case NEW_VERSIONED_COMMENT:
					case NEW_REPLY:
					case REMOVED_GENERAL_COMMENT:
					case REMOVED_VERSIONED_COMMENT:
					case REMOVED_REPLY:
						AbstractCommentNotification commentNotification = (AbstractCommentNotification) notification;
						if (!commentNotification.isDraft()) {
							changesCount++;
							notificationCount++;
							changes.append("<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\"")
									.append(notification.getItemUrl()).append("\">")
									.append(id).append("</a></td><td>")
									.append(notification.getPresentationMessage()).append("</td></tr>");
						}
						break;
					case UPDATED_GENERAL_COMMENT:
					case UPDATED_VERSIONED_COMMENT:
					case UPDATED_REPLY:
						AbstractUpdatedCommentNotification updatedCommentNotification
								= (AbstractUpdatedCommentNotification) notification;
						if (!updatedCommentNotification.isDraft()
								&& (!updatedCommentNotification.isDraft() && !updatedCommentNotification.wasDraft())) {
							changesCount++;
							notificationCount++;
							changes.append("<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\"")
									.append(notification.getItemUrl()).append("\">")
									.append(id).append("</a></td><td>")
									.append(notification.getPresentationMessage()).append("</td></tr>");
						}
						break;
					default:
						changesCount++;
						notificationCount++;
						changes.append("<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\"")
								.append(notification.getItemUrl()).append("\">")
								.append(id).append("</a></td><td>")
								.append(notification.getPresentationMessage()).append("</td></tr>");
						break;
				}
			}
			return this;
		}
	}
}
