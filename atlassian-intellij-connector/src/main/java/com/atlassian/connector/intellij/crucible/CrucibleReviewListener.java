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

package com.atlassian.connector.intellij.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;

import java.util.List;

public interface CrucibleReviewListener {

	void createdOrEditedVersionedCommentReply(ReviewAdapter review, PermId file,
			VersionedComment parentComment, VersionedComment comment);

	void createdOrEditedGeneralCommentReply(ReviewAdapter review, GeneralComment parentComment,
			GeneralComment comment);


	void createdOrEditedGeneralComment(ReviewAdapter review, GeneralComment comment);

	void createdOrEditedVersionedComment(ReviewAdapter review,
			PermId file,
			VersionedComment comment);

	void removedComment(ReviewAdapter review, Comment comment);

	void publishedGeneralComment(ReviewAdapter review, GeneralComment comment);

	void publishedVersionedComment(ReviewAdapter review, PermId filePermId,
			VersionedComment comment);

	/**
	 * Notifies that something has been changed in the review
	 *
	 * @param reviewAdapter changed review
	 * @param notifications list of changes in review
	 */
	void reviewChanged(final ReviewAdapter reviewAdapter,
			final List<CrucibleNotification> notifications);

    void commentReadStateChanged(ReviewAdapter review, Comment comment);
}
