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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.intellij.openapi.project.Project;

public class CrucibleCompleteWorker {
    private ReviewAdapter reviewInfo;
    private boolean complete;
	private Project project;

	public CrucibleCompleteWorker(final Project project, ReviewAdapter reviewInfo, boolean complete) {
        this.reviewInfo = reviewInfo;
        this.complete = complete;
		this.project = project;
	}

    public void run() {
        final CrucibleChangeReviewStateForm reviewCompleteForm =
                new CrucibleChangeReviewStateForm(project, reviewInfo,
						complete ? CrucibleAction.COMPLETE : CrucibleAction.UNCOMPLETE);
        reviewCompleteForm.showDialog();
    }
}