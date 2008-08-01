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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public class NewVersionedCommentNotification extends AbstractReviewNotification {
	private VersionedComment comment;

	public NewVersionedCommentNotification(ReviewData review, VersionedComment comment) {
		super(review);
		this.comment = comment;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.NEW_VERSIONED_COMMENT;
	}

	public String getPresentationMessage() {
		return "New comment added by " + comment.getAuthor().getDisplayName();
	}

	public ReviewData getReview() {
		return review;
	}

	public CrucibleFileInfo getReviewItem() {
		try {
			for (CrucibleFileInfo file : review.getFiles()) {
				if (comment.getReviewItemId().equals(file.getPermId())) {
					return file;
				}
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			valueNotYetInitialized.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return null;
	}
}