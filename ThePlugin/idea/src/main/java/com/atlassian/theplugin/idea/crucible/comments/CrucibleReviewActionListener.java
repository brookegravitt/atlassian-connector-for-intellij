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
import com.atlassian.theplugin.idea.crucible.ReviewData;

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
    public void showReview(ReviewData reviewData) {
    }

    public void focusOnGeneralComments(ReviewData review) {
    }

    public void focusOnFileComments(ReviewData review, CrucibleFileInfo file) {
    }

	public void showFile(ReviewData review, CrucibleFileInfo file) {
	}

	public void aboutToAddVersionedCommentReply(ReviewData review, CrucibleFileInfo file,
            VersionedComment parentComment, VersionedCommentBean newComment) {
	}

	public void createdVersionedCommentReply(ReviewData review, CrucibleFileInfo file,
            VersionedComment parentComment, VersionedComment comment) {
	}

	public void aboutToAddGeneralCommentReply(ReviewData review, GeneralComment parentComment, GeneralCommentBean newComment) {
	}

	public void createdGeneralCommentReply(ReviewData review, GeneralComment parentComment, GeneralComment comment) {
	}

	public void aboutToAddGeneralComment(ReviewData review, GeneralCommentBean newComment) {
	}

	public void createdGeneralComment(ReviewData review, GeneralComment comment) {
	}

	public void aboutToAddVersionedComment(ReviewData review, CrucibleFileInfo file, VersionedCommentBean comment) {
	}
}
