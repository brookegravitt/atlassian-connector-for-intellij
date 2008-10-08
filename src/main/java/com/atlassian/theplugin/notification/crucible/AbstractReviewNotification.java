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

import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

public abstract class AbstractReviewNotification implements CrucibleNotification {
    protected Review review;

    public AbstractReviewNotification(Review review) {
        this.review = review;
    }

    public abstract CrucibleNotificationType getType();

    public PermId getId() {
        return review.getPermId();
    }

    public String getItemUrl() {
        return review.getReviewUrl();
    }

    public abstract String getPresentationMessage();
}