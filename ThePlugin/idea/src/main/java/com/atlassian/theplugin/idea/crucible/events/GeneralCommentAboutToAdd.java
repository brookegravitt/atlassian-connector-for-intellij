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

import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 24, 2008
 * Time: 5:38:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralCommentAboutToAdd extends CrucibleEvent {
	private ReviewAdapter review;
	private GeneralCommentBean newComment;

	public GeneralCommentAboutToAdd(CrucibleReviewActionListener caller, ReviewAdapter review,
			GeneralCommentBean newComment) {
		super(caller);
		this.review = review;
		this.newComment = newComment;
	}

	protected void notify(CrucibleReviewActionListener listener) {
		listener.aboutToAddGeneralComment(review, newComment);
	}
}
