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

import com.atlassian.theplugin.commons.configuration.CrucibleTooltipOption;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.crucible.api.model.notification.*;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusIcon;
import com.intellij.openapi.project.Project;
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
	private final PluginConfiguration pluginConfiguration;

	private final Map<ServerData, NewExceptionNotification> exceptions = new HashMap<ServerData, NewExceptionNotification>();

	public CrucibleNotificationTooltip(@NotNull final CrucibleStatusIcon display, @NotNull final Project project,
			@NotNull final PluginToolWindow pluginToolWindow, final PluginConfiguration pluginConfiguration) {
		this.display = display;
		this.project = project;
		this.pluginToolWindow = pluginToolWindow;
		this.pluginConfiguration = pluginConfiguration;
	}


	//private List<CrucibleNotification>
	public void updateNotifications(java.util.List<CrucibleNotification> notifications) {
		boolean exceptionRaised = false;
		if (!notifications.isEmpty() && pluginConfiguration.getCrucibleConfigurationData().getCrucibleTooltipOption()
				!= CrucibleTooltipOption.NEVER) {
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
					content.setText("<html>" + Constants.BODY_WITH_STYLE + sb.toString() + "</body></html>");
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
					IdeaVersionFacade.getInstance().fireNotification(
							project,
							new JScrollPane(content),
							content.getText(),
							"/icons/crucible-blue-16.png",
							newExceptionCount > 0
									? IdeaVersionFacade.OperationStatus.ERROR
									: IdeaVersionFacade.OperationStatus.INFO,
							BACKGROUND_COLOR);
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
				String id = "";
				if (notification.getId() != null) {
					id = notification.getId().getId();
				}

				switch (notification.getType()) {
					case EXCEPTION_RAISED:
					case NEW_REVIEW:
					case NOT_VISIBLE_REVIEW:
					case REVIEW_DATA_CHANGED:
						break;
					case NEW_COMMENT:
					case REMOVED_COMMENT:
						AbstractCommentNotification commentNotification = (AbstractCommentNotification) notification;
						if (!commentNotification.getComment().isDraft()) {
							changesCount++;
							notificationCount++;
							changes.append("<tr><td colspan=2 width=\"1%\" nowrap valign=top><a href=\"")
									.append(notification.getItemUrl()).append("\">")
									.append(id).append("</a></td><td>")
									.append(notification.getPresentationMessage()).append("</td></tr>");
						}
						break;
					case UPDATED_COMMENT:
						AbstractUpdatedCommentNotification updatedCommentNotification
								= (AbstractUpdatedCommentNotification) notification;
						if (!updatedCommentNotification.getComment().isDraft()
								&& (!updatedCommentNotification.getComment().isDraft() && !updatedCommentNotification.wasDraft())) {
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
