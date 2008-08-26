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

package com.atlassian.theplugin.commons.crucible.api.model;

public enum Action {
    VIEW("View review", "action:viewReview"),
    CREATE("Create review", "action:createReview"),
    ABANDON("Abandon review", "action:abandonReview"),
    SUBMIT("Submit review", "action:submitReview"),
    APPROVE("Approve review", "action:approveReview"),
    REJECT("Reject review", "action:rejectReview"),
    SUMMARIZE("Summarize review", "action:summarizeReview"),
    CLOSE("Close review", "action:closeReview"),
    REOPEN("Reopen review", "action:reopenReview"),
    RECOVER("Recover review", "action:recoverReview"),
    COMPLETE("Complete review", "action:completeReview"),
    UNCOMPLETE("Uncomplete review", "action:uncompleteReview"),
    COMMENT("Comment", "action:commentOnReview"),
    MODIFYFILES("Modify files", "action:modifyReviewFiles"),
    DELETE("Delete review", "action:deleteReview");

    private final String displayName;
    private final String actionName;

    Action(String dName, String aName) {
        displayName = dName;
        actionName = aName;
    }

    public String displayName() {
        return displayName;
    }

    public String actionName() {
        return actionName;
    }

    public static Action fromValue(String v) {
        for (Action c : Action.values()) {
            if (c.actionName.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}