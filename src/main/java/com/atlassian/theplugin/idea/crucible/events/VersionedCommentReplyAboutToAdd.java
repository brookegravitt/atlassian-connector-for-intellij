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
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 11:23:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedCommentReplyAboutToAdd extends CrucibleEvent {
    private ReviewData review;
    private CrucibleFileInfo file;
    private VersionedComment parentComment;
    private VersionedCommentBean newComment;

    public VersionedCommentReplyAboutToAdd(CrucibleReviewActionListener caller, ReviewData review, CrucibleFileInfo file,
                                           VersionedComment parentComment, VersionedCommentBean newComment) {
        super(caller);
        this.review = review;
        this.file = file;
        this.parentComment = parentComment;
        this.newComment = newComment;
    }

    protected void notify(CrucibleReviewActionListener listener) {
        listener.aboutToAddVersionedCommentReply(review, file, parentComment, newComment);
    }
}
