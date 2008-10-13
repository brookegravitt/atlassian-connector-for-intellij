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

import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;

public class ReviewerCompletedNotification extends AbstractReviewNotification {
    private Reviewer reviewer;

    public ReviewerCompletedNotification(ReviewDataImpl review, Reviewer reviewer) {
        super(review);
        this.reviewer = reviewer;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.REVIEW_COMPLETED;
    }

    public String getPresentationMessage() {
        return "Reviewer " + reviewer.getDisplayName() + " " + (reviewer.isCompleted() ? "completed" : " uncompleted")
                + " review";
    }
}