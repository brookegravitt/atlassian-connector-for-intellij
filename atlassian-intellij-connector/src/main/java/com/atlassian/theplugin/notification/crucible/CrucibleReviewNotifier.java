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

import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListener;
import com.atlassian.theplugin.crucible.model.UpdateContext;
import com.atlassian.theplugin.crucible.model.UpdateReason;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This one is supposed to be per project.
 */
public class CrucibleReviewNotifier implements CrucibleStatusListener, CrucibleReviewListModelListener {
	private final List<CrucibleNotificationListener> listenerList = new ArrayList<CrucibleNotificationListener>();

	private boolean firstRun = true;
	private Project project;


	public CrucibleReviewNotifier(@NotNull final Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void registerListener(CrucibleNotificationListener listener) {
		synchronized (listenerList) {
			if (!listenerList.contains(listener)) {
				listenerList.add(listener);
			}
		}
	}

	public void unregisterListener(CrucibleNotificationListener listener) {
		synchronized (listenerList) {
			listenerList.remove(listener);
		}
	}

	public void showError(String errorString) {
		// ignore
	}

	public void resetState() {
		for (CrucibleNotificationListener listener : listenerList) {
			listener.resetState();
		}
	}

	public void reviewAdded(UpdateContext updateContext) {
	}

	public void reviewRemoved(UpdateContext updateContext) {
	}

	public void reviewChanged(UpdateContext updateContext) {
	}

	public void modelChanged(UpdateContext updateContext) {
	}

	public void reviewListUpdateStarted(UpdateContext updateContext) {
	}

	public void reviewListUpdateFinished(UpdateContext updateContext) {
		if (!firstRun) {
			if (canAddNotifiaction(updateContext)) {
				List<CrucibleNotification> notifications = updateContext.getNotifications();
				if (notifications != null && !notifications.isEmpty()) {
					for (CrucibleNotificationListener listener : listenerList) {
						listener.updateNotifications(notifications);
					}
				}
			}
		}
		firstRun = false;
	}

	public void reviewListUpdateError(UpdateContext updateContext, Exception exception) {
	}

	private boolean canAddNotifiaction(UpdateContext updateContext) {
		return (updateContext.getUpdateReason() == UpdateReason.REFRESH
				|| updateContext.getUpdateReason() == UpdateReason.TIMER_FIRED);


	}
}


