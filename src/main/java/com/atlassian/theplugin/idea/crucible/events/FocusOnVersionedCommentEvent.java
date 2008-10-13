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

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Aug 5, 2008
 * Time: 12:07:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class FocusOnVersionedCommentEvent extends CrucibleEvent {
	private ReviewDataImpl review;
	private CrucibleFileInfo file;
	private VersionedComment comment;

	public FocusOnVersionedCommentEvent(final CrucibleReviewActionListener caller, final ReviewDataImpl review,
			final CrucibleFileInfo file, final VersionedComment comment) {
		super(caller);
		this.review = review;
		this.file = file;
		this.comment = comment;
	}

	protected void notify(final CrucibleReviewActionListener listener) {
		listener.focusOnVersionedCommentEvent(review, file, comment);
	}
}
