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
import com.atlassian.theplugin.idea.crucible.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;

public class VersionedCommentUpdated extends CrucibleEvent {
    private ReviewAdapter review;
    private CrucibleFileInfo fileInfo;
    private VersionedComment comment;

    public VersionedCommentUpdated(final CrucibleReviewActionListener caller, final ReviewAdapter review,
            final CrucibleFileInfo fileInfo, final VersionedComment comment) {
        super(caller);
        this.review = review;
        this.fileInfo = fileInfo;
        this.comment = comment;
    }

    protected void notify(final CrucibleReviewActionListener listener) {
        listener.updatedVersionedComment(review, fileInfo, comment);
    }
}
