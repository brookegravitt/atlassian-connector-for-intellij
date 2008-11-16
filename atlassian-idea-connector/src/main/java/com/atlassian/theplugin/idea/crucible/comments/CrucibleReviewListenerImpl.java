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

package com.atlassian.theplugin.idea.crucible.comments;

import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.api.model.*;

public class CrucibleReviewListenerImpl implements CrucibleReviewListener {
	public static final CrucibleReviewListener I_WANT_THIS_MESSAGE_BACK = null;
	public static final CrucibleReviewListener ANONYMOUS = null;

	public void createdOrEditedVersionedCommentReply(ReviewAdapter review, PermId file,
			VersionedComment parentComment, VersionedComment comment) {
	}

	public void createdOrEditedGeneralCommentReply(ReviewAdapter review, GeneralComment parentComment,
			GeneralComment comment) {
	}

	public void createdOrEditedGeneralComment(ReviewAdapter review, GeneralComment comment) {
	}


	public void createdOrEditedVersionedComment(ReviewAdapter review,
			PermId file, VersionedComment comment) {
	}

	public void removedComment(final ReviewAdapter review, final Comment comment) {
	}

	public void publishedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
	}

	public void publishedVersionedComment(final ReviewAdapter review, final PermId filePermId,
			final VersionedComment comment) {
	}

	public void reviewUpdated(final ReviewAdapter newReview) {
	}

}
