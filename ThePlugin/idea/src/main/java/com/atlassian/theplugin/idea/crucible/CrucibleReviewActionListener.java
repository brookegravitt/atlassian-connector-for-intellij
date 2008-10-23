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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.intellij.openapi.editor.Editor;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 16, 2008
 * Time: 7:04:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CrucibleReviewActionListener {
	void showReview(ReviewAdapter reviewData);

	void focusOnGeneralComments(ReviewAdapter review);

	void focusOnFileComments(ReviewAdapter review, CrucibleFileInfo file);

	void showFile(ReviewAdapter review, CrucibleFileInfo file);

	void showDiff(CrucibleFileInfo file);

	void aboutToAddLineComment(ReviewAdapter review, CrucibleFileInfo file, Editor editor, int start, int end);

	void aboutToAddVersionedCommentReply(ReviewAdapter review, CrucibleFileInfo file,
			VersionedComment parentComment, VersionedComment newComment);

	void createdOrEditedVersionedCommentReply(ReviewAdapter review, CrucibleReviewItemInfo file,
			VersionedComment parentComment, VersionedComment comment);

	void aboutToAddGeneralCommentReply(ReviewAdapter review,
			GeneralComment parentComment,
			GeneralComment newComment);

	void createdOrEditedGeneralCommentReply(ReviewAdapter review, GeneralComment parentComment,
			GeneralComment comment);

	void aboutToAddGeneralComment(ReviewAdapter review, GeneralComment newComment);

	void createdOrEditedGeneralComment(ReviewAdapter review, GeneralComment comment);

	void aboutToAddVersionedComment(ReviewAdapter review,
			CrucibleFileInfo file,
			VersionedComment comment);

	void createdOrEditedVersionedComment(ReviewAdapter review,
			CrucibleReviewItemInfo file,
			VersionedComment comment);

	void aboutToUpdateVersionedComment(ReviewAdapter review, CrucibleFileInfo file,
			VersionedComment comment);

	void aboutToUpdateGeneralComment(ReviewAdapter review, GeneralComment comment);

	void updatedVersionedComment(ReviewAdapter review,
			CrucibleReviewItemInfo file,
			VersionedComment comment);

	void updatedGeneralComment(ReviewAdapter review, GeneralComment comment);

	void aboutToRemoveComment(ReviewAdapter review, Comment comment);

	void removedComment(ReviewAdapter review, Comment comment);

	void focusOnVersionedCommentEvent(ReviewAdapter review, CrucibleFileInfo file,
			VersionedComment comment);

	void focusOnLineCommentEvent(ReviewAdapter review, CrucibleFileInfo file,
			VersionedComment comment, boolean openIfClosed);

	void aboutToPublishGeneralComment(ReviewAdapter review, GeneralComment comment);

	void aboutToPublishVersionedComment(ReviewAdapter review, CrucibleFileInfo file,
			VersionedComment comment);

	void publishedGeneralComment(ReviewAdapter review, GeneralComment comment);

	void publishedVersionedComment(ReviewAdapter review, CrucibleReviewItemInfo file,
			VersionedComment comment);

	void commentsDownloaded(ReviewAdapter review);
}
