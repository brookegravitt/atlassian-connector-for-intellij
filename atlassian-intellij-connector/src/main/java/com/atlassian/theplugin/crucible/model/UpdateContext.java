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

package com.atlassian.theplugin.crucible.model;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;

import java.util.List;

public class UpdateContext {
	private final UpdateReason updateReason;

	private final ReviewAdapter reviewAdapter;

	private final List<CrucibleNotification> notifications;

	public UpdateContext(final UpdateReason updateReason, final ReviewAdapter reviewAdapter,
			final List<CrucibleNotification> notifications) {
		this.updateReason = updateReason;
		this.reviewAdapter = reviewAdapter;
		this.notifications = notifications;
	}

	public UpdateReason getUpdateReason() {
		return updateReason;
	}

	public ReviewAdapter getReviewAdapter() {
		if (reviewAdapter == null) {
			throw new IllegalStateException();
		}
		return reviewAdapter;
	}

	public List<CrucibleNotification> getNotifications() {
		return notifications;
	}
}
