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

package com.atlassian.theplugin.idea.crucible.events;


import com.atlassian.theplugin.commons.crucible.api.model.CrucibleReviewItemInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;


public class VersionedCommentAddedOrEdited extends CrucibleEvent {
	private ReviewAdapter review;
	private CrucibleReviewItemInfo file;
	private VersionedComment comment;

	public VersionedCommentAddedOrEdited(CrucibleReviewActionListener caller, ReviewAdapter review,
			CrucibleReviewItemInfo file,
			VersionedComment newComment) {
		super(caller);
		this.review = review;
		this.file = file;
		this.comment = newComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.createdOrEditedVersionedComment(review, file, comment);
	}
}
