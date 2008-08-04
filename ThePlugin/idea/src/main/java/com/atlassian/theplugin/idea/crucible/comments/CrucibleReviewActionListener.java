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

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
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
    public void showReview(ReviewData reviewData) {
    }

    public void focusOnGeneralComments(ReviewData review) {
    }

    public void focusOnFileComments(ReviewData review, CrucibleFileInfo file) {
    }

    public void showFile(ReviewData review, CrucibleFileInfo file) {
    }

    public void showDiff(CrucibleFileInfo file) {
    }

    public void aboutToAddLineComment(ReviewData review, CrucibleFileInfo file, Editor editor, int start, int end) {
    }

    public void aboutToAddVersionedCommentReply(ReviewData review, CrucibleFileInfo file,
                                                VersionedComment parentComment, VersionedComment newComment) {
    }

    public void createdVersionedCommentReply(ReviewData review, CrucibleFileInfo file,
                                             VersionedComment parentComment, VersionedComment comment) {
    }

    public void aboutToAddGeneralCommentReply(ReviewData review,
                                              GeneralComment parentComment,
                                              GeneralComment newComment) {
    }

    public void createdGeneralCommentReply(ReviewData review, GeneralComment parentComment, GeneralComment comment) {
    }

    public void aboutToAddGeneralComment(ReviewData review, GeneralComment newComment) {
    }

    public void createdGeneralComment(ReviewData review, GeneralComment comment) {
    }

    public void aboutToAddVersionedComment(ReviewData review,
                                           CrucibleFileInfo file,
                                           VersionedComment comment,
                                           Editor editor) {
    }

    public void createdVersionedComment(ReviewData review,
                                        CrucibleFileInfo file,
                                        VersionedComment comment,
                                        Editor editor) {
    }

    public void aboutToUpdateVersionedComment(final ReviewData review, final CrucibleFileInfo file,
                                              final VersionedComment comment) {
    }

    public void aboutToUpdateGeneralComment(final ReviewData review, final GeneralComment comment) {
    }

    public void updatedVersionedComment(final ReviewData review,
                                        final CrucibleFileInfo file,
                                        final VersionedComment comment) {
    }

    public void updatedGeneralComment(final ReviewData review, final GeneralComment comment) {
    }

    public void aboutToRemoveComment(final ReviewData review, final Comment comment) {
    }

    public void removedComment(final ReviewData review, final Comment comment) {
    }

}
