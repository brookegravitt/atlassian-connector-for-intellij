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

package com.atlassian.theplugin.notification.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.idea.crucible.ReviewData;

public class NewReplyCommentNotification extends AbstractReviewNotification {
    private Comment comment;
    private Comment reply;

    public NewReplyCommentNotification(ReviewData review, Comment comment, Comment reply) {
        super(review);
        this.comment = comment;
        this.reply = reply;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.NEW_REPLY;
    }

    public String getPresentationMessage() {
        return "New reply added by " + reply.getUser().getDisplayName();
    }
}