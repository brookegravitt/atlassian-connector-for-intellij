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

import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;
import com.intellij.openapi.editor.Editor;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 16, 2008
 * Time: 10:15:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleReviewActionListener {
	public static final CrucibleReviewActionListener I_WANT_THIS_MESSAGE_BACK = null;
	public static final CrucibleReviewActionListener ANONYMOUS = null;

	/**
	 * A method ivoked by a background thread when a new review needs to be shown
	 *
	 * @param reviewData
	 */
	public void showReview(ReviewDataImpl reviewData) {
	}

	public void focusOnGeneralComments(ReviewDataImpl review) {
	}

	public void focusOnFileComments(ReviewDataImpl review, CrucibleFileInfo file) {
	}

	public void showFile(ReviewDataImpl review, CrucibleFileInfo file) {
	}

	public void showDiff(CrucibleFileInfo file) {
	}

	public void aboutToAddLineComment(ReviewDataImpl review, CrucibleFileInfo file, Editor editor, int start, int end) {
	}

	public void aboutToAddVersionedCommentReply(ReviewDataImpl review, CrucibleFileInfo file,
			VersionedComment parentComment, VersionedComment newComment) {
	}

	public void createdOrEditedVersionedCommentReply(ReviewDataImpl review, CrucibleReviewItemInfo file,
			VersionedComment parentComment, VersionedComment comment) {
	}

	public void aboutToAddGeneralCommentReply(ReviewDataImpl review,
			GeneralComment parentComment,
			GeneralComment newComment) {
	}

	public void createdOrEditedGeneralCommentReply(ReviewDataImpl review, GeneralComment parentComment, GeneralComment comment) {
	}

	public void aboutToAddGeneralComment(ReviewDataImpl review, GeneralComment newComment) {
	}

	public void createdOrEditedGeneralComment(ReviewDataImpl review, GeneralComment comment) {
	}

	public void aboutToAddVersionedComment(ReviewDataImpl review,
			CrucibleFileInfo file,
			VersionedComment comment) {
	}

	public void createdOrEditedVersionedComment(ReviewDataImpl review,
			CrucibleReviewItemInfo file,
			VersionedComment comment) {
	}

	public void aboutToUpdateVersionedComment(final ReviewDataImpl review, final CrucibleFileInfo file,
			final VersionedComment comment) {
	}

	public void aboutToUpdateGeneralComment(final ReviewDataImpl review, final GeneralComment comment) {
	}

	public void updatedVersionedComment(final ReviewDataImpl review,
			final CrucibleReviewItemInfo file,
			final VersionedComment comment) {
	}

	public void updatedGeneralComment(final ReviewDataImpl review, final GeneralComment comment) {
	}

	public void aboutToRemoveComment(final ReviewDataImpl review, final Comment comment) {
	}

	public void removedComment(final ReviewDataImpl review, final Comment comment) {
	}

	public void focusOnFile(final ReviewDataImpl review, final CrucibleFileInfo file) {
	}

	public void focusOnReview(final ReviewDataImpl review) {
	}

	public void focusOnVersionedCommentEvent(final ReviewDataImpl review, final CrucibleFileInfo file,
			final VersionedComment comment) {
	}

	public void focusOnLineCommentEvent(final ReviewDataImpl review, final CrucibleFileInfo file, final VersionedComment comment,
			final boolean openIfClosed) {
	}

	public void aboutToPublishGeneralComment(final ReviewDataImpl review, final GeneralComment comment) {
	}

	public void aboutToPublishVersionedComment(final ReviewDataImpl review, final CrucibleFileInfo file,
			final VersionedComment comment) {
	}

	public void publishedGeneralComment(final ReviewDataImpl review, final GeneralComment comment) {
	}

	public void publishedVersionedComment(final ReviewDataImpl review, final CrucibleReviewItemInfo file,
			final VersionedComment comment) {
	}

	public void commentsDownloaded(ReviewDataImpl review) {
	}
}
