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
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;
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
	public void showReview(ReviewAdapter reviewData) {
	}

	public void focusOnGeneralComments(ReviewAdapter review) {
	}

	public void focusOnFileComments(ReviewAdapter review, CrucibleFileInfo file) {
	}

	public void showFile(ReviewAdapter review, CrucibleFileInfo file) {
	}

	public void showDiff(CrucibleFileInfo file) {
	}

	public void aboutToAddLineComment(ReviewAdapter review, CrucibleFileInfo file, Editor editor, int start, int end) {
	}

	public void aboutToAddVersionedCommentReply(ReviewAdapter review, CrucibleFileInfo file,
			VersionedComment parentComment, VersionedComment newComment) {
	}

	public void createdOrEditedVersionedCommentReply(ReviewAdapter review, CrucibleReviewItemInfo file,
			VersionedComment parentComment, VersionedComment comment) {
	}

	public void aboutToAddGeneralCommentReply(ReviewAdapter review,
			GeneralComment parentComment,
			GeneralComment newComment) {
	}

	public void createdOrEditedGeneralCommentReply(ReviewAdapter review, GeneralComment parentComment,
			GeneralComment comment) {
	}

	public void aboutToAddGeneralComment(ReviewAdapter review, GeneralComment newComment) {
	}

	public void createdOrEditedGeneralComment(ReviewAdapter review, GeneralComment comment) {
	}

	public void aboutToAddVersionedComment(ReviewAdapter review,
			CrucibleFileInfo file,
			VersionedComment comment) {
	}

	public void createdOrEditedVersionedComment(ReviewAdapter review,
			CrucibleReviewItemInfo file,
			VersionedComment comment) {
	}

	public void aboutToUpdateVersionedComment(final ReviewAdapter review, final CrucibleFileInfo file,
			final VersionedComment comment) {
	}

	public void aboutToUpdateGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
	}

	public void updatedVersionedComment(final ReviewAdapter review,
			final CrucibleReviewItemInfo file,
			final VersionedComment comment) {
	}

	public void updatedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
	}

	public void aboutToRemoveComment(final ReviewAdapter review, final Comment comment) {
	}

	public void removedComment(final ReviewAdapter review, final Comment comment) {
	}

	public void focusOnFile(final ReviewAdapter review, final CrucibleFileInfo file) {
	}

	public void focusOnReview(final ReviewAdapter review) {
	}

	public void focusOnVersionedCommentEvent(final ReviewAdapter review, final CrucibleFileInfo file,
			final VersionedComment comment) {
	}

	public void focusOnLineCommentEvent(final ReviewAdapter review, final CrucibleFileInfo file,
			final VersionedComment comment,	final boolean openIfClosed) {
	}

	public void aboutToPublishGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
	}

	public void aboutToPublishVersionedComment(final ReviewAdapter review, final CrucibleFileInfo file,
			final VersionedComment comment) {
	}

	public void publishedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
	}

	public void publishedVersionedComment(final ReviewAdapter review, final CrucibleReviewItemInfo file,
			final VersionedComment comment) {
	}

	public void commentsDownloaded(ReviewAdapter review) {
	}
}
